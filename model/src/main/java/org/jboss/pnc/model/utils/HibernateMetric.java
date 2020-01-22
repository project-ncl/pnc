package org.jboss.pnc.model.utils;

import java.io.Serializable;

public class HibernateMetric implements Serializable {

    private static final long serialVersionUID = -8183319087958223970L;

    private String name;
    private String description;
    private String value;

    public HibernateMetric(String name, String description, long value) {
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
        return new StringBuilder().append("[name=").append(name).append(",description=").append(description).append(",value=")
                .append(value).append(']').toString();
    }
}
