/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.converter;

public class Value<DB, T> {
    private final Class<DB> modelClass;
    private final String name;
    private final Class<T> javaType;
    private final String value;

    public Value(Class<DB> modelClass, String name, Class<T> javaType, String value) {
        this.modelClass = modelClass;
        this.name = name;
        this.javaType = javaType;
        this.value = value;
    }

    public Class<DB> getModelClass() {
        return modelClass;
    }

    public String getName() {
        return name;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    public String getValue() {
        return value;
    }
}
