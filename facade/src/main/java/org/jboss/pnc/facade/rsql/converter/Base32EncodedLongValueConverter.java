/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.converter;

import org.jboss.pnc.facade.rsql.RSQLException;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.GenericEntity;

public class Base32EncodedLongValueConverter implements ValueConverter {

    @Override
    public <DB extends GenericEntity<?>, T> Comparable<T> convertComparable(Value<DB, T> value) {
        throw new RSQLException("Comparing by id is not supported.");
    }

    @Override
    public <DB extends GenericEntity<?>, T> T convert(Value<DB, T> value) {
        if (value.getJavaType() != Base32LongID.class) {
            throw new IllegalArgumentException(
                    "Expected to get value for type Base32LongID, got value for type " + value.getJavaType());
        }

        return (T) new Base32LongID(value.getValue());
    }
}
