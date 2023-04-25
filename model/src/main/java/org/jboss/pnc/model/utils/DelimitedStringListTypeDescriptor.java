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

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores a List of Strings as a SINGLE string delimited by DELIMITER
 *
 * @see <a href=
 *      "https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#collections-as-basic">Inspiration</a>
 */
public class DelimitedStringListTypeDescriptor extends AbstractTypeDescriptor<List> {

    public static final String DELIMITER = ";";

    public DelimitedStringListTypeDescriptor() {
        super(List.class, new MutableMutabilityPlan<>() {
            @Override
            protected List deepCopyNotNull(List value) {
                return new ArrayList(value);
            }
        });
    }

    @Override
    public String toString(List value) {
        return String.join(DELIMITER, ((List<String>) value));
    }

    @Override
    public List fromString(String string) {
        List<String> values = new ArrayList<>();
        Collections.addAll(values, string.split(DELIMITER));
        return values;
    }

    @Override
    public <X> X unwrap(List value, Class<X> type, WrapperOptions options) {
        return (X) toString(value);
    }

    @Override
    public <X> List wrap(X value, WrapperOptions options) {
        return fromString((String) value);
    }
}
