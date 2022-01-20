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
package org.jboss.pnc.model;

import java.util.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.jboss.pnc.enums.BuildPushStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(indexes = { @Index(name = "idx_buildrecordpushresult_buildrecord", columnList = "buildRecord_id") })
public class BuildRecordPushResult implements GenericEntity<Long> {
    private static final long serialVersionUID = 8461294730832773438L;

    @Id
    @NotNull
    private Long id;

    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecordpushresult_buildrecord"))
    @ManyToOne
    private BuildRecord buildRecord;

    @Enumerated(EnumType.STRING)
    private BuildPushStatus status;

    @Deprecated
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

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_pushresult_milestonerelease"))
    private ProductMilestoneRelease productMilestoneRelease;

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
        setProductMilestoneRelease(builder.productMilestoneRelease);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    public void setBuildRecord(BuildRecord buildRecord) {
        this.buildRecord = buildRecord;
    }

    public BuildPushStatus getStatus() {
        return status;
    }

    public void setStatus(BuildPushStatus status) {
        this.status = status;
    }

    @Deprecated
    public String getLog() {
        return log;
    }

    @Deprecated
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

    public ProductMilestoneRelease getProductMilestoneRelease() {
        return productMilestoneRelease;
    }

    public void setProductMilestoneRelease(ProductMilestoneRelease productMilestoneRelease) {
        this.productMilestoneRelease = productMilestoneRelease;
    }

    @Override
    public String toString() {
        return "BuildRecordPushResult{" + "id=" + id + ", buildRecord=" + buildRecord + ", status=" + status + ", log='"
                + log + '\'' + ", brewBuildId=" + brewBuildId + ", brewBuildUrl='" + brewBuildUrl + '\''
                + ", tagPrefix='" + tagPrefix + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BuildRecordPushResult))
            return false;
        return id != null && id.equals(((BuildRecordPushResult) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static final class Builder {

        private Long id;

        private BuildRecord buildRecord;

        private BuildPushStatus status;

        @Deprecated
        private String log;

        private Integer brewBuildId;

        private String brewBuildUrl;

        private String tagPrefix;

        private ProductMilestoneRelease productMilestoneRelease;

        private Builder() {
        }

        public Builder id(Long val) {
            id = val;
            return this;
        }

        public Builder buildRecord(BuildRecord val) {
            buildRecord = val;
            return this;
        }

        public Builder status(BuildPushStatus val) {
            status = val;
            return this;
        }

        @Deprecated
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

        public Builder productMilestoneRelease(ProductMilestoneRelease productMilestoneRelease) {
            this.productMilestoneRelease = productMilestoneRelease;
            return this;
        }

        public BuildRecordPushResult build() {
            return new BuildRecordPushResult(this);
        }
    }
}
