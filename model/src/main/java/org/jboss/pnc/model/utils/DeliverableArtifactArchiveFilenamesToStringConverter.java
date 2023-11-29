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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

@Converter
public class DeliverableArtifactArchiveFilenamesToStringConverter
        implements AttributeConverter<Collection<String>, String> {

    private static final String DELIMITER = ";";

    @Override
    public String convertToDatabaseColumn(Collection<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(DELIMITER);
        attribute.forEach(joiner::add);

        return joiner.toString();
    }

    @Override
    public Collection<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        String[] elements = dbData.split(DELIMITER);
        return List.of(elements);
    }
}