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
