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
package org.jboss.pnc.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "build_record_attributes")
@IdClass(BuildRecordAttribute.AttributeId.class)
public class BuildRecordAttribute implements Serializable {

    // specify the column name for backwards compatibility with how it was named in PNC 1.x
    @Id
    @Column(name = "build_record_id")
    @ManyToOne
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

        Integer buildRecordId;

        String key;

        public AttributeId() {
        }

        public AttributeId(BuildRecord buildRecord, String key) {
            this.buildRecordId = buildRecord.getId();
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            AttributeId that = (AttributeId) o;
            return buildRecordId.equals(that.buildRecordId) && key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(buildRecordId, key);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BuildRecordAttribute that = (BuildRecordAttribute) o;
        return buildRecord.equals(that.buildRecord) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildRecord, key);
    }
}
