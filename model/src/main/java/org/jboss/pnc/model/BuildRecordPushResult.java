/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Entity
@Table(indexes = {@Index(name = "idx_buildrecordpushresult_buildrecord", columnList = "buildRecord_id")})
public class BuildRecordPushResult implements GenericEntity<Integer> {
    private static final long serialVersionUID = 8461294730832773438L;

    public static final String SEQUENCE_NAME = "build_record_push_result_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecordpushresult_buildrecord"))
    @ManyToOne
    private BuildRecord buildRecord;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String log;

    /**
     * build id assigned by brew
     */
    private Integer brewBuildId;

    /**
     * link to brew
     */
    private String brewBuildUrl;

    private String tagPrefix;

    public BuildRecordPushResult() {
    }

    private BuildRecordPushResult(Builder builder) {
        setId(builder.id);
        setBuildRecord(builder.buildRecord);
        setStatus(builder.status);
        setLog(builder.log);
        setBrewBuildId(builder.brewBuildId);
        setBrewBuildUrl(builder.brewBuildUrl);
        setTagPrefix(builder.tagPrefix);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public enum Status {
        SUCCESS, FAILED, SYSTEM_ERROR, CANCELED;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    public void setBuildRecord(BuildRecord buildRecord) {
        this.buildRecord = buildRecord;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Integer getBrewBuildId() {
        return brewBuildId;
    }

    public void setBrewBuildId(Integer brewBuildId) {
        this.brewBuildId = brewBuildId;
    }

    public String getBrewBuildUrl() {
        return brewBuildUrl;
    }

    public void setBrewBuildUrl(String brewBuildUrl) {
        this.brewBuildUrl = brewBuildUrl;
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    @Override
    public String toString() {
        return "BuildRecordPushResult{" +
                "id=" + id +
                ", buildRecord=" + buildRecord +
                ", status=" + status +
                ", log='" + log + '\'' +
                ", brewBuildId=" + brewBuildId +
                ", brewBuildUrl='" + brewBuildUrl + '\'' +
                ", tagPrefix='" + tagPrefix + '\'' +
                '}';
    }

    public static final class Builder {

        private Integer id;

        private BuildRecord buildRecord;

        private Status status;

        private String log;

        private Integer brewBuildId;

        private String brewBuildUrl;

        private String tagPrefix;

        private Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder buildRecord(BuildRecord val) {
            buildRecord = val;
            return this;
        }

        public Builder status(Status val) {
            status = val;
            return this;
        }

        public Builder log(String val) {
            log = val;
            return this;
        }

        public Builder brewBuildId(Integer val) {
            brewBuildId = val;
            return this;
        }

        public Builder brewBuildUrl(String val) {
            brewBuildUrl = val;
            return this;
        }

        public Builder tagPrefix(String val) {
            tagPrefix = val;
            return this;
        }

        public BuildRecordPushResult build() {
            return new BuildRecordPushResult(this);
        }
    }
}
