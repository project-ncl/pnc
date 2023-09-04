/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.model.utils;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.EnumSet;

/**
 * The converter between {@link DeliverableAnalyzerReportLabel} and {@link String}. It automatically converts the
 * attribute annotated with {@link javax.persistence.Convert} to {@link String}. When we're fetching the data from the
 * database, the opposite conversion takes place. Hence, we do not need to create extra-table just for the labels.
 */
@Converter(autoApply = true)
public class DeliverableAnalyzerReportLabelToStringConverter
        implements AttributeConverter<EnumSet<DeliverableAnalyzerReportLabel>, String> {

    private static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(EnumSet<DeliverableAnalyzerReportLabel> labelsSet) {
        var sb = new StringBuilder();
        var sep = "";

        for (var label : labelsSet) {
            sb.append(sep).append(label.name());
            sep = SEPARATOR;
        }

        return sb.toString();
    }

    @Override
    public EnumSet<DeliverableAnalyzerReportLabel> convertToEntityAttribute(String labelsString) {
        var setOfLabels = EnumSet.noneOf(DeliverableAnalyzerReportLabel.class);
        var labelsSplit = labelsString.split(SEPARATOR);

        for (String label : labelsSplit) {
            setOfLabels.add(Enum.valueOf(DeliverableAnalyzerReportLabel.class, label));
        }

        return setOfLabels;
    }
}