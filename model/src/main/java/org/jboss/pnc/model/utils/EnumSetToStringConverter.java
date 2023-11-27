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
import java.util.EnumSet;
import java.util.StringJoiner;

/**
 * Generic converter which converts between EnumSet<E> and String. It automatically converts the attribute annotated
 * with {@link javax.persistence.Convert} to {@link String}. When we're fetching the data from the database, the
 * opposite conversion takes place. Hence, we do not need to create extra-table just for the enum set.
 * <p>
 * Note: We want to make this generic class abstract in order to prevent instantiation, e.g. due to forgotten
 * {@code @Converter} annotation.
 * </p>
 *
 * @param <E> entity class
 */
public abstract class EnumSetToStringConverter<E extends Enum<E>> implements AttributeConverter<EnumSet<E>, String> {

    private final Class<E> enumType;

    private static final String SEPARATOR = ",";

    public EnumSetToStringConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(EnumSet<E> labelsSet) {
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        for (E label : labelsSet) {
            joiner.add(label.name());
        }
        return joiner.toString();
    }

    @Override
    public EnumSet<E> convertToEntityAttribute(String labelsString) {
        if (labelsString == null || "".equals(labelsString)) {
            return EnumSet.noneOf(enumType);
        }

        var setOfLabels = EnumSet.noneOf(enumType);
        var labelsSplit = labelsString.split(SEPARATOR);

        for (String label : labelsSplit) {
            setOfLabels.add(Enum.valueOf(enumType, label));
        }

        return setOfLabels;
    }
}
