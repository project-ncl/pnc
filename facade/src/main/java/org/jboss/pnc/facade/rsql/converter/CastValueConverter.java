/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.converter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.jboss.pnc.facade.rsql.RSQLException;
import org.jboss.pnc.model.GenericEntity;

public class CastValueConverter implements ValueConverter {

    @Override
    public <DB extends GenericEntity<?>, T> Comparable<T> convertComparable(Value<DB, T> value) {
        Class<T> javaType = value.getJavaType();
        String argument = value.getValue();

        if (javaType.isEnum()) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) javaType;
            return (Comparable<T>) Enum.valueOf(enumType, argument);
        } else if (javaType == String.class) {
            return (Comparable<T>) argument;
        } else if (javaType == Integer.class || javaType == int.class) {
            return (Comparable<T>) Integer.valueOf(argument);
        } else if (javaType == Long.class || javaType == long.class) {
            return (Comparable<T>) Long.valueOf(argument);
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return (Comparable<T>) Boolean.valueOf(argument);
        } else if (javaType == Date.class) {
            try {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(argument, timeFormatter);
                return (Comparable<T>) Date.from(Instant.from(offsetDateTime));
            } catch (DateTimeParseException ex) {
                throw new RSQLException(
                        "The datetime must be in the ISO-8601 format with timezone, e.g. 1970-01-01T00:00:00Z, was "
                                + argument,
                        ex);
            }
        } else if (GenericEntity.class.isAssignableFrom(javaType)) {
            throw new RSQLException("Entity type " + javaType + " is not comparable. Specify an entity attribute.");
        } else {
            throw new UnsupportedOperationException(
                    "The target type " + javaType + " is not known to the type converter.");
        }
    }

    @Override
    public <DB extends GenericEntity<?>, T> T convert(Value<DB, T> value) {
        return (T) convertComparable(value);
    }
}
