/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "build_record_attributes")
@IdClass(BuildRecordAttribute.AttributeId.class)
public class BuildRecordAttribute implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "build_record_id")
    private BuildRecord buildRecord;

    @Id
    private String key;

    private String value;

    public BuildRecordAttribute() {
    }

    public BuildRecordAttribute(BuildRecord buildRecord, String key, String value) {
        this.buildRecord = buildRecord;
        this.key = key;
        this.value = value;
    }

    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setBuildRecord(BuildRecord buildRecord) {
        this.buildRecord = buildRecord;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class AttributeId implements Serializable {

        BuildRecord buildRecord;

        String key;

        public AttributeId() {
        }

        public AttributeId(BuildRecord buildRecord, String key) {
            this.buildRecord = buildRecord;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            AttributeId that = (AttributeId) o;
            return buildRecord.getId().equals(that.buildRecord.getId()) && key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(buildRecord.getId(), key);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BuildRecordAttribute))
            return false;
        BuildRecordAttribute that = (BuildRecordAttribute) o;
        return key.equals(that.getKey()) && buildRecord.getId().equals(that.getBuildRecord().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildRecord.getId(), key);
    }
}
