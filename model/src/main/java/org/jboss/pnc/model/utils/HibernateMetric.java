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

import java.io.Serializable;
import java.util.Objects;

public class HibernateMetric implements Serializable {

    private static final long serialVersionUID = -8183319087958223970L;

    private String name;
    private String description;
    private String value;

    public HibernateMetric(String name, String description, double value) {
        this.name = name;
        this.description = description;
        this.value = String.valueOf(value);
    }

    public HibernateMetric(String name, String description, String value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[name=")
                .append(name)
                .append(",description=")
                .append(description)
                .append(",value=")
                .append(value)
                .append(']')
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HibernateMetric metric = (HibernateMetric) o;
        return Objects.equals(name, metric.getName()) && Objects.equals(description, metric.getDescription())
                && Objects.equals(value, metric.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, value);
    }

}
