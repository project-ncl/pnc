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
package org.jboss.pnc.model;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.utils.DeliverableAnalyzerReportLabelToStringConverter;
import org.junit.Test;

import java.util.EnumSet;

public class DeliverableAnalyzerReportLabelToStringConverterTest extends AbstractModelTest {

    private DeliverableAnalyzerReportLabelToStringConverter converter = new DeliverableAnalyzerReportLabelToStringConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        EnumSet<DeliverableAnalyzerReportLabel> entityLabels = EnumSet
                .of(DeliverableAnalyzerReportLabel.RELEASED, DeliverableAnalyzerReportLabel.SCRATCH);

        var dbLabels = converter.convertToDatabaseColumn(entityLabels);

        Assertions.assertThat(dbLabels).isEqualTo("SCRATCH,RELEASED");
    }

    @Test
    public void testConvertToEntityAttribute() {
        String dbLabels = "SCRATCH,DELETED";

        EnumSet<DeliverableAnalyzerReportLabel> entityLabels = converter.convertToEntityAttribute(dbLabels);

        Assertions.assertThat(entityLabels)
                .isEqualTo(EnumSet.of(DeliverableAnalyzerReportLabel.DELETED, DeliverableAnalyzerReportLabel.SCRATCH));
    }
}