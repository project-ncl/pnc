/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model.utils;

import java.util.EnumSet;
import java.util.StringJoiner;

import javax.persistence.AttributeConverter;

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
