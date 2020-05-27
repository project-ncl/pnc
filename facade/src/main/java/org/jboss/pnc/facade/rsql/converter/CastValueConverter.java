/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.converter;

import org.jboss.pnc.facade.rsql.RSQLException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class CastValueConverter implements ValueConverter {

    @Override
    public <DB, T> Comparable<T> convert(Value<DB, T> value) {
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
        } else {
            throw new UnsupportedOperationException(
                    "The target type " + javaType + " is not known to the type converter.");
        }
    }
}
