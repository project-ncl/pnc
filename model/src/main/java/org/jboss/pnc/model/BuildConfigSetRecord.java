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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildStatus;

import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceException;
import javax.persistence.PreRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains a summary of the build results of the execution of a build config set. This includes the start
 * and end time, links to the build records for the executed builds, and the overall status (success/failure) of the set
 * execution.
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        indexes = { @Index(name = "idx_buildconfigsetrecord_buildconfigset", columnList = "buildconfigurationset_id"),
                @Index(name = "idx_buildconfigsetrecord_productversion", columnList = "productversion_id"),
                @Index(name = "idx_buildconfigsetrecord_user", columnList = "user_id") })
public class BuildConfigSetRecord implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String SEQUENCE_NAME = "build_config_set_record_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * The build configuration set which was executed
     */
    @NotNull
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfigsetrecord_buildconfigset"))
    private BuildConfigurationSet buildConfigurationSet;

    /**
     * The time at which the first build in the set was started
     */
    @NotNull
    @Column(columnDefinition = "timestamp with time zone")
    private Date startTime;

    /**
     * The time at which the last build in the set was completed Temporarily set to null while the set is executing
     */
    @Column(columnDefinition = "timestamp with time zone")
    private Date endTime;

    /**
     * The user who executed the set.
     */
    @NotNull
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfigsetrecord_user"))
    private User user;

    /**
     * The status (success/failure) of the overall set. If any builds in the set failed, the status of the set is
     * failed.
     */
    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    /**
     * The detailed records of the builds that were executed as part of the execution of this set
     *
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "buildConfigSetRecord")
    private Set<BuildRecord> buildRecords;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfigsetrecord_productversion"))
    private ProductVersion productVersion;

    @NotNull
    private boolean temporaryBuild;

    @Enumerated(EnumType.STRING)
    private AlignmentPreference alignmentPreference;

    /**
     * Example attributes POST_BUILD_REPO_VALIDATION: REPO_SYSTEM_ERROR
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "build_config_set_record_attributes",
            joinColumns = @JoinColumn(
                    name = "build_config_set_record_id",
                    foreignKey = @ForeignKey(name = "fk_build_config_set_record_attributes_build_config_set_record")))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> attributes = new HashMap<>();

    /**
     * Instantiates a new project build result.
     */
    public BuildConfigSetRecord() {
        buildRecords = new HashSet<>();
    }

    @PreRemove
    public void preRemove() {
        if (!this.temporaryBuild)
            throw new PersistenceException(
                    "The non-temporary builds cannot be deleted! Only deletion of temporary builds is supported");
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the new start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the new end time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the new user
     */
    public void setUser(User user) {
        this.user = user;
    }

    public BuildConfigurationSet getBuildConfigurationSet() {
        return buildConfigurationSet;
    }

    public void setBuildConfigurationSet(BuildConfigurationSet buildConfigurationSet) {
        this.buildConfigurationSet = buildConfigurationSet;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public BuildStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    public void setBuildRecords(Set<BuildRecord> buildRecords) {
        this.buildRecords = buildRecords;
    }

    public boolean addBuildRecord(BuildRecord buildRecord) {
        buildRecord.setBuildConfigSetRecord(this);
        return buildRecords.add(buildRecord);
    }

    public boolean removeBuildRecord(BuildRecord buildRecord) {
        buildRecord.setBuildConfigSetRecord(null);
        return buildRecords.remove(buildRecord);
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the system image.
     *
     * @param productVersion the new system image
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
    }

    public boolean isTemporaryBuild() {
        return temporaryBuild;
    }

    public void setTemporaryBuild(boolean temporaryBuild) {
        this.temporaryBuild = temporaryBuild;
    }

    public AlignmentPreference getAlignmentPreference() {
        return alignmentPreference;
    }

    public void setAlignmentPreference(AlignmentPreference alignmentPreference) {
        this.alignmentPreference = alignmentPreference;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "BuildConfigSetRecord [id=" + id + ", buildConfigurationSet=" + buildConfigurationSet.getName()
                + ", status=" + status + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BuildConfigSetRecord))
            return false;
        return id != null && id.equals(((BuildConfigSetRecord) o).getId());
    }

    @Override
    public int hashCode() {
        // Because the id is generated when the entity is stored to DB, we need to have constant hash code to achieve
        // equals+hashCode consistency across all JPA object states
        return 31;
    }

    public static class Builder {

        private Integer id;

        private BuildConfigurationSet buildConfigurationSet;

        private Date startTime;

        private Date endTime;

        private BuildStatus status;

        private User user;

        private ProductVersion productVersion;

        private Set<BuildRecord> buildRecords;

        private Boolean temporaryBuild;

        private AlignmentPreference alignmentPreference;

        public Builder() {
            buildRecords = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildConfigSetRecord build() {
            BuildConfigSetRecord buildConfigSetRecord = new BuildConfigSetRecord();
            buildConfigSetRecord.setId(id);
            buildConfigSetRecord.setBuildConfigurationSet(buildConfigurationSet);
            buildConfigSetRecord.setStartTime(startTime);
            buildConfigSetRecord.setEndTime(endTime);
            buildConfigSetRecord.setUser(user);
            buildConfigSetRecord.setStatus(status);
            buildConfigSetRecord.setTemporaryBuild(temporaryBuild);
            buildConfigSetRecord.setAlignmentPreference(alignmentPreference);

            if (productVersion == null && buildConfigurationSet != null) {
                productVersion = buildConfigurationSet.getProductVersion();
            }
            buildConfigSetRecord.setProductVersion(productVersion);

            // Set the bi-directional mapping
            for (BuildRecord buildRecord : buildRecords) {
                buildRecord.setBuildConfigSetRecord(buildConfigSetRecord);
            }
            buildConfigSetRecord.setBuildRecords(buildRecords);

            return buildConfigSetRecord;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder buildConfigurationSet(BuildConfigurationSet buildConfigurationSet) {
            this.buildConfigurationSet = buildConfigurationSet;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder status(BuildStatus status) {
            this.status = status;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder buildRecords(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

        public Builder temporaryBuild(boolean temporaryBuild) {
            this.temporaryBuild = temporaryBuild;
            return this;
        }

        public Builder alignmentPreference(AlignmentPreference alignmentPreference) {
            this.alignmentPreference = alignmentPreference;
            return this;
        }
    }

}
