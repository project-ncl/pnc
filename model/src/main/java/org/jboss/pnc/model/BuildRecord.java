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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.jboss.pnc.common.security.Md5;
import org.jboss.pnc.common.security.Sha256;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.enums.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * <p>
 * This class contains the build result of a project configuration, and contains additional metadata, as the build
 * script, the starting and ending time of a build, the status of the build, the sources url used, the user that
 * triggered the build, plus all the Artifacts that were built and all the Artifacts that were used for the final build.
 * It stores also the buildDriverID that was used to run the build, the system Image where is was run in, and is mapped
 * to a BuildRecordSet, that encapsulates the set of buildRecord that compose a Product
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        indexes = { @Index(name = "idx_buildrecord_user", columnList = "user_id"),
                @Index(name = "idx_buildrecord_buildenvironment", columnList = "buildenvironment_id"),
                @Index(name = "idx_buildrecord_buildconfigsetrecord", columnList = "buildconfigsetrecord_id"),
                @Index(name = "idx_buildrecord_buildconfiguration", columnList = "buildconfiguration_id"),
                @Index(
                        name = "idx_buildrecord_buildconfiguration_aud",
                        columnList = "buildconfiguration_id,buildconfiguration_rev"),
                @Index(name = "idx_buildrecord_productmilestone", columnList = "productmilestone_id"),
                @Index(name = "idx_buildrecord_norebuildcause", columnList = "norebuildcause_id") })
public class BuildRecord implements GenericEntity<Base32LongID> {

    private static final long serialVersionUID = -5472083609387609797L;

    public static final String SEQUENCE_NAME = "build_record_id_seq";

    private static Logger logger = LoggerFactory.getLogger(BuildRecord.class);

    @EmbeddedId
    private Base32LongID id;

    /**
     * Contains the settings that were used at the time the build was executed. Hibernate envers identifies each audited
     * record using the "id" of the original db record along with a revision number. This can be used to re-run the
     * build with the exact same settings used previously.
     */
    @Transient
    private BuildConfigurationAudited buildConfigurationAudited;

    @NotNull
    @Column(name = "buildconfiguration_id", updatable = false)
    private Integer buildConfigurationId;

    @NotNull
    @Column(name = "buildconfiguration_rev", updatable = false)
    private Integer buildConfigurationRev;

    @Size(max = 100)
    @Column(updatable = false)
    private String buildContentId;

    @NotNull
    @Column(updatable = false)
    private boolean temporaryBuild;

    /**
     * The time which the build was submitted to the system.
     */
    @NotNull
    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date submitTime;

    /**
     * The time when the build execution started. Note that it's possible for this to be null in the case of a system
     * error before the build is started.
     */
    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date startTime;

    /**
     * The time when the build completed. Note that it's possible for this to be null if the build never finished.
     */
    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date endTime;

    @NotNull
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecord_user"), updatable = false)
    private User user;

    /**
     * The scm repository URL used for executing the build. Note, this can be different than the repository URL
     * contained in the linked build configuration due to pre-build processing tasks such as repository mirroring and
     * automated build changes.
     */
    @Size(max = 255)
    @Column(updatable = false)
    private String scmRepoURL;

    /**
     * The scm revision used for build execution. Note, this can be different than the revision submitted by the user
     * due to automated build processing steps which modify the sources before executing the build. This should always
     * be an unmodifiable commit ID and should never be a tag or branch.
     */
    @Size(max = 255)
    @Column(updatable = false)
    private String scmRevision;

    /**
     * The SCM revision in human readable form such as Git Tag.
     */
    @Size(max = 255)
    @Column(updatable = false)
    private String scmTag;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    @LazyGroup("buildLog")
    private String buildLog;

    private String buildLogMd5;

    private String buildLogSha256;

    private Integer buildLogSize;

    /**
     * Checksum of build logs. Used to verify the integrity of the logs in the remote storage eg. Elasticsearch
     */
    @Column(updatable = false)
    private String buildOutputChecksum;

    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    @Size(max = 150)
    @Column(updatable = false)
    private String sshCommand;

    @Size(max = 64)
    @Column(updatable = false)
    private String sshPassword;

    /**
     * This is an identifier of the built project sources. In case of Maven, it is GA of the POM being built. This
     * information comes from Repour/PME and has to be stored in the build record to be used in the release process.
     */
    @Size(max = 255)
    private String executionRootName;

    /**
     * See {@link BuildRecord#executionRootName}. Contains corresponding version.
     */
    @Size(max = 100)
    private String executionRootVersion;

    /**
     * Artifacts which were produced by this build
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "buildRecord")
    private Set<Artifact> builtArtifacts;

    /**
     * Artifacts which are required external dependencies of this build
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    @JoinTable(
            name = "build_record_artifact_dependencies_map",
            joinColumns = { @JoinColumn(
                    name = "build_record_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_record_artifact_dependencies_map_buildrecord")) },
            inverseJoinColumns = { @JoinColumn(
                    name = "dependency_artifact_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_record_artifact_dependencies_map_dependency")) },
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_build_record_id_dependency_artifact_id",
                    columnNames = { "build_record_id", "dependency_artifact_id" }),
            indexes = { @Index(
                    name = "idx_build_record_artifact_dependencies_map",
                    columnList = "dependency_artifact_id") })
    @Column(updatable = false)
    private Set<Artifact> dependencies;

    /**
     * Environment configuration (including system image) that was used to instantiate the build host.
     *
     * @deprecated environment is linked via build configuration audited
     */
    @Deprecated
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecord_buildenvironment"), updatable = false)
    private BuildEnvironment buildEnvironment;

    /**
     * The product milestone for which this build was performed. Even though the artifacts from this build may be
     * included in multiple product milestones/releases, there should only be a single primary product milestone which
     * originally produced this build.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecord_productMilestone"), updatable = false)
    private ProductMilestone productMilestone;

    /**
     * If this build was executed as part of a set, this will contain the link to the overall results of the set.
     * Otherwise, this field will be null.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecord_buildconfigsetrecord"), updatable = true)
    private BuildConfigSetRecord buildConfigSetRecord;

    /**
     * Example attributes POST_BUILD_REPO_VALIDATION: REPO_SYSTEM_ERROR
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "buildRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<BuildRecordAttribute> attributes = new HashSet<>();

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    @LazyGroup("repourLog")
    @Column(updatable = false)
    private String repourLog;

    @Column(updatable = false)
    private String repourLogMd5;

    @Column(updatable = false)
    private String repourLogSha256;

    @Column(updatable = false)
    private Integer repourLogSize;

    @OneToMany(mappedBy = "buildRecord", cascade = CascadeType.REMOVE)
    private Set<BuildRecordPushResult> buildRecordPushResults;

    /**
     * A collection of buildRecords that depends on this at time this is stored. Dependents are defined based on
     * scheduled state.
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String dependentBuildRecordIds;

    /**
     * A collection of buildRecords that this depends on at time this is stored. Dependencies are defined based on
     * scheduled state.
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String dependencyBuildRecordIds;

    /**
     * In case of status NO_REBUILD_REQUIRED, this field references the BuildRecord that caused the decision of not
     * rebuilding.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecord_norebuildcause"), updatable = false)
    private BuildRecord noRebuildCause;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp with time zone")
    private Date lastUpdateTime;

    /**
     * Instantiates a new project build result.
     */
    public BuildRecord() {
        dependencies = new HashSet<>();
        builtArtifacts = new HashSet<>();
    }

    @PreRemove
    public void preRemove() {
        if (!this.temporaryBuild)
            throw new PersistenceException(
                    "The non-temporary builds cannot be deleted! Only deletion of temporary builds is supported");
    }

    @PrePersist
    public void prePersist() {
        if (this.temporaryBuild && this.productMilestone != null) {
            logger.warn("Temporary builds cannot be assigned to a milestone");
            this.productMilestone = null;
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Base32LongID getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
    public void setId(Base32LongID id) {
        this.id = id;
    }

    /**
     * The time when the build was submitted.
     *
     * @return the submit time
     */
    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * The time when the build execution was started. The build task wait time can be determined by the difference
     * between the startTime and the submitTime.
     *
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the time when the build finished. The build duration can be determined by the difference between the endTime
     * and the startTime.
     *
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

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

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public String getScmTag() {
        return scmTag;
    }

    public void setScmTag(String scmTag) {
        this.scmTag = scmTag;
    }

    public String getRepourLog() {
        return repourLog;
    }

    /**
     *
     * @deprecated use builder instead
     */
    @Deprecated
    public void setRepourLog(String repourLog) {
        this.repourLog = repourLog;
    }

    /**
     * Gets the builds the log.
     *
     * @return the builds the log
     */
    public String getBuildLog() {
        return buildLog;
    }

    /**
     * Sets the builds the log.
     *
     * @param buildLog the new builds the log
     *
     * @deprecated use builder instead
     */
    @Deprecated
    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
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

    /**
     * Gets the built artifacts.
     *
     * @return the built artifacts
     */
    public Set<Artifact> getBuiltArtifacts() {
        return builtArtifacts;
    }

    public void addArtifact(Artifact artifact) {
        builtArtifacts.add(artifact);
        artifact.setBuildRecord(this);
    }

    public void removeArtifact(Artifact artifact) {
        builtArtifacts.remove(artifact);
        artifact.setBuildRecord(null);
    }

    /**
     * Gets the dependencies.
     *
     * @return the dependencies
     */
    public Set<Artifact> getDependencies() {
        return dependencies;
    }

    public void addDependency(Artifact artifact) {
        dependencies.add(artifact);
    }

    /**
     * Sets the dependencies.
     *
     * @param dependencies the new dependencies
     */
    public void setDependencies(Set<Artifact> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Gets the build environment.
     *
     * @return the environment settings used on the build host
     */
    @Deprecated
    public BuildEnvironment getBuildEnvironment() {
        return buildEnvironment;
    }

    /**
     * Sets the build environment.
     *
     * @param buildEnvironment the build environment configuration
     */
    @Deprecated
    public void setBuildEnvironment(BuildEnvironment buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    /**
     * @return The audited version of the build configuration used to create this build record
     */
    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public void setBuildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
        setBuildConfigurationAuditedIfValid(
                this,
                buildConfigurationId,
                buildConfigurationRev,
                buildConfigurationAudited);
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
        if (buildConfigurationAudited != null && !buildConfigurationId.equals(buildConfigurationAudited.getId())) {
            buildConfigurationAudited = null;
            logger.warn("Removing transient BuildConfigurationAudited as its id does not match.");
        }
    }

    public void setBuildConfigurationRev(Integer buildConfigurationRev) {
        this.buildConfigurationRev = buildConfigurationRev;
        if (buildConfigurationAudited != null && !buildConfigurationRev.equals(buildConfigurationAudited.getRev())) {
            buildConfigurationAudited = null;
            logger.warn("Removing transient BuildConfigurationAudited as its revision does not match.");
        }
    }

    public IdRev getBuildConfigurationAuditedIdRev() {
        return new IdRev(buildConfigurationId, buildConfigurationRev);
    }

    /**
     * The product milestone for which this build was performed
     *
     * @return The product milestone
     */
    public ProductMilestone getProductMilestone() {
        return productMilestone;
    }

    public void setProductMilestone(ProductMilestone productMilestone) {
        this.productMilestone = productMilestone;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    /**
     * @param buildContentId The identifier to use when accessing repository or other content stored via external
     *        services.
     */
    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public BuildConfigSetRecord getBuildConfigSetRecord() {
        return buildConfigSetRecord;
    }

    public void setBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        this.buildConfigSetRecord = buildConfigSetRecord;
    }

    @Override
    public String toString() {
        return "BuildRecord [id=" + id + ", buildConfiguration=" + buildConfigurationAudited + ", status=" + status
                + "]";
    }

    public Set<BuildRecordAttribute> getAttributes() {
        return attributes;
    }

    public Map<String, String> getAttributesMap() {
        return attributes.stream()
                .collect(Collectors.toMap(BuildRecordAttribute::getKey, BuildRecordAttribute::getValue));
    }

    public void setAttributes(Set<BuildRecordAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        Optional<BuildRecordAttribute> attribute = getAttributeEntity(key);
        if (attribute.isPresent()) {
            return attribute.get().getValue();
        } else {
            return null;
        }
    }

    public Optional<BuildRecordAttribute> getAttributeEntity(String key) {
        return attributes.stream().filter(a -> a.getKey().equals(key)).findAny();
    }

    public String putAttribute(String key, String value) {
        if (id == null) {
            throw new PersistenceException("Build record id must be set before adding the attributes.");
        }
        Optional<BuildRecordAttribute> old = getAttributeEntity(key);
        BuildRecordAttribute attribute = new BuildRecordAttribute(this, key, value);
        old.ifPresent(attributes::remove);
        attributes.add(attribute);
        return old.map(BuildRecordAttribute::getValue).orElse(null);
    }

    public void removeAttribute(String key) {
        Optional<BuildRecordAttribute> attribute = getAttributeEntity(key);
        attribute.ifPresent(attributes::remove);
        attribute.ifPresent(a -> a.setBuildRecord(null));
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public Integer getBuildConfigurationRev() {
        return buildConfigurationRev;
    }

    public boolean isTemporaryBuild() {
        return temporaryBuild;
    }

    public void setTemporaryBuild(boolean temporaryBuild) {
        this.temporaryBuild = temporaryBuild;
    }

    public String getBuildLogMd5() {
        return buildLogMd5;
    }

    public void setBuildLogMd5(String buildLogMd5) {
        this.buildLogMd5 = buildLogMd5;
    }

    public String getBuildLogSha256() {
        return buildLogSha256;
    }

    public void setBuildLogSha256(String buildLogSha256) {
        this.buildLogSha256 = buildLogSha256;
    }

    public Integer getBuildLogSize() {
        return buildLogSize;
    }

    public void setBuildLogSize(Integer buildLogSize) {
        this.buildLogSize = buildLogSize;
    }

    public String getbuildOutputChecksum() {
        return buildOutputChecksum;
    }

    public void setbuildOutputChecksum(String buildOutputChecksum) {
        this.buildOutputChecksum = buildOutputChecksum;
    }

    public String getSshCommand() {
        return sshCommand;
    }

    public void setSshCommand(String sshCommand) {
        this.sshCommand = sshCommand;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword = sshPassword;
    }

    public String getExecutionRootName() {
        return executionRootName;
    }

    public void setExecutionRootName(String executionRootName) {
        this.executionRootName = executionRootName;
    }

    public String getExecutionRootVersion() {
        return executionRootVersion;
    }

    public void setExecutionRootVersion(String executionRootVersion) {
        this.executionRootVersion = executionRootVersion;
    }

    public String getRepourLogMd5() {
        return repourLogMd5;
    }

    public void setRepourLogMd5(String repourLogMd5) {
        this.repourLogMd5 = repourLogMd5;
    }

    public String getRepourLogSha256() {
        return repourLogSha256;
    }

    public void setRepourLogSha256(String repourLogSha256) {
        this.repourLogSha256 = repourLogSha256;
    }

    public Integer getRepourLogSize() {
        return repourLogSize;
    }

    public void setRepourLogSize(Integer repourLogSize) {
        this.repourLogSize = repourLogSize;
    }

    public Set<BuildRecordPushResult> getBuildRecordPushResults() {
        return buildRecordPushResults;
    }

    public void setBuildRecordPushResults(Set<BuildRecordPushResult> buildRecordPushResults) {
        this.buildRecordPushResults = buildRecordPushResults;
    }

    public void addBuildRecordPushResult(BuildRecordPushResult buildRecordPushResult) {
        buildRecordPushResults.add(buildRecordPushResult);
        buildRecordPushResult.setBuildRecord(this);
    }

    public void removeBuildRecordPushResult(BuildRecordPushResult buildRecordPushResult) {
        buildRecordPushResults.remove(buildRecordPushResult);
        buildRecordPushResult.setBuildRecord(null);
    }

    public void setDependentBuildRecordIds(Base32LongID[] dependentBuildRecordIds) {
        if (dependentBuildRecordIds != null) {
            Long[] longIds = Arrays.stream(dependentBuildRecordIds).map(Base32LongID::getLongId).toArray(Long[]::new);
            this.dependentBuildRecordIds = StringUtils.serializeLong(longIds);
        } else {
            this.dependentBuildRecordIds = "";
        }
    }

    public Base32LongID[] getDependentBuildRecordIds() {
        Long[] longIds = StringUtils.deserializeLong(dependentBuildRecordIds);
        return Arrays.stream(longIds).map(Base32LongID::new).toArray(Base32LongID[]::new);
    }

    public Base32LongID[] getDependencyBuildRecordIds() {
        Long[] longIds = StringUtils.deserializeLong(dependencyBuildRecordIds);
        return Arrays.stream(longIds).map(Base32LongID::new).toArray(Base32LongID[]::new);
    }

    public void setDependencyBuildRecordIds(Base32LongID[] dependencyBuildRecordIds) {
        if (dependencyBuildRecordIds != null) {
            Long[] longIds = Arrays.stream(dependencyBuildRecordIds).map(Base32LongID::getLongId).toArray(Long[]::new);
            this.dependencyBuildRecordIds = StringUtils.serializeLong(longIds);
        } else {
            this.dependencyBuildRecordIds = "";
        }
    }

    private static void setBuildConfigurationAuditedIfValid(
            BuildRecord buildRecord,
            Integer buildConfigurationAuditedId,
            Integer buildConfigurationAuditedRev,
            BuildConfigurationAudited buildConfigurationAudited) {
        if (buildConfigurationAuditedId == null && buildConfigurationAuditedRev == null) {
            buildRecord.buildConfigurationId = buildConfigurationAudited.getId();
            buildRecord.buildConfigurationRev = buildConfigurationAudited.getRev();
            buildRecord.buildConfigurationAudited = buildConfigurationAudited;
        } else if (buildConfigurationAuditedId != null || buildConfigurationAuditedRev != null) {
            // if audited has same idRev as manually set, then set the audited object
            if (buildConfigurationAuditedId.equals(buildConfigurationAudited.getId())
                    && buildConfigurationAuditedRev.equals(buildConfigurationAudited.getRev())) {
                buildRecord.buildConfigurationAudited = buildConfigurationAudited;
            } else {
                logger.warn("Trying to set BuildConfigurationAudited with invalid idRev.");
            }
        }
    }

    public BuildRecord getNoRebuildCause() {
        return noRebuildCause;
    }

    public void setNoRebuildCause(BuildRecord noRebuildCause) {
        this.noRebuildCause = noRebuildCause;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BuildRecord))
            return false;
        return id != null && id.equals(((BuildRecord) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static class Builder {

        private Base32LongID id;

        private String buildContentId;

        private Boolean temporaryBuild;

        private Date submitTime;

        private Date startTime;

        private Date endTime;

        private BuildConfigurationAudited buildConfigurationAudited;

        private Integer buildConfigurationAuditedId;

        private Integer buildConfigurationAuditedRev;

        private User user;

        private String scmRepoURL;

        private String scmRevision;

        private String scmTag;

        private String repourLog = "";

        private String buildLog = "";

        private String buildOutputChecksum;

        private BuildStatus status;

        private Set<Artifact> dependencies;

        private BuildEnvironment buildEnvironment;

        private ProductMilestone productMilestone;

        private BuildConfigSetRecord buildConfigSetRecord;

        private String sshCommand;

        private String sshPassword;

        private String executionRootName;

        private String executionRootVersion;

        private Map<String, String> attributes = new HashMap<>();

        private Base32LongID[] dependentBuildRecordIds;

        private Base32LongID[] dependencyBuildRecordIds;

        private BuildRecord noRebuildCause;

        private Date lastUpdateTime;

        public Builder() {
            dependencies = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecord build() {
            return build(true);
        }

        /**
         *
         * @param sanitizeLogs required because PostgreSQL doesn't support storing NULL (\0x00) characters in text
         *        fields"
         * @return
         */
        public BuildRecord build(boolean sanitizeLogs) {
            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setId(id);
            buildRecord.setBuildContentId(buildContentId);
            buildRecord.setSubmitTime(submitTime);
            buildRecord.setStartTime(startTime);
            buildRecord.setEndTime(endTime);
            buildRecord.setLastUpdateTime(lastUpdateTime);
            buildRecord.setUser(user);
            buildRecord.setScmRepoURL(scmRepoURL);
            buildRecord.setScmRevision(scmRevision);
            buildRecord.setScmTag(scmTag);
            buildRecord.setStatus(status);
            buildRecord.setBuildEnvironment(buildEnvironment);
            buildRecord.setSshCommand(sshCommand);
            buildRecord.setSshPassword(sshPassword);
            buildRecord.setExecutionRootName(executionRootName);
            buildRecord.setExecutionRootVersion(executionRootVersion);
            buildRecord.setBuildConfigurationId(buildConfigurationAuditedId);
            buildRecord.setBuildConfigurationRev(buildConfigurationAuditedRev);
            setLogs(buildRecord, sanitizeLogs);
            buildRecord.setbuildOutputChecksum(buildOutputChecksum);

            if (temporaryBuild == null) {
                temporaryBuild = true;
            }
            buildRecord.setTemporaryBuild(temporaryBuild);

            if (!temporaryBuild) {
                buildRecord.setProductMilestone(productMilestone);
            }

            if (buildConfigurationAudited != null) {
                setBuildConfigurationAuditedIfValid(
                        buildRecord,
                        buildConfigurationAuditedId,
                        buildConfigurationAuditedRev,
                        buildConfigurationAudited);
            }

            if (buildConfigSetRecord != null) {
                buildRecord.setBuildConfigSetRecord(buildConfigSetRecord);
            }

            buildRecord.setDependencies(dependencies);

            buildRecord.setDependentBuildRecordIds(dependentBuildRecordIds);
            buildRecord.setDependencyBuildRecordIds(dependencyBuildRecordIds);

            Set<BuildRecordAttribute> buildRecordAttributes = attributes.entrySet()
                    .stream()
                    .map(kv -> new BuildRecordAttribute(buildRecord, kv.getKey(), kv.getValue()))
                    .collect(Collectors.toSet());
            buildRecord.setAttributes(buildRecordAttributes);
            buildRecord.setNoRebuildCause(noRebuildCause);

            return buildRecord;
        }

        private void setLogs(BuildRecord buildRecord, boolean sanitizeLogs) {
            try {
                if (repourLog != null) {
                    if (sanitizeLogs) {
                        buildRecord.setRepourLog(repourLog.replaceAll("\u0000", ""));
                    } else {
                        buildRecord.setRepourLog(repourLog);
                    }
                    buildRecord.setRepourLogSize(buildRecord.repourLog.getBytes(UTF_8).length);
                    buildRecord.setRepourLogMd5(Md5.digest(buildRecord.repourLog));
                    buildRecord.setRepourLogSha256(Sha256.digest(buildRecord.repourLog));
                }
                if (buildLog != null) {
                    if (sanitizeLogs) {
                        buildRecord.setBuildLog(buildLog.replaceAll("\u0000", ""));
                    } else {
                        buildRecord.setBuildLog(buildLog);
                    }
                    buildRecord.setBuildLogSize(buildRecord.buildLog.getBytes(UTF_8).length);
                    buildRecord.setBuildLogMd5(Md5.digest(buildRecord.buildLog));
                    buildRecord.setBuildLogSha256(Sha256.digest(buildRecord.buildLog));
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                logger.error("Cannot compute log checksum.", e);
                throw new RuntimeException("Cannot compute log checksum.", e);
            }
        }

        public Builder id(Base32LongID id) {
            this.id = id;
            return this;
        }

        public Builder id(String id) {
            this.id = new Base32LongID(id);
            return this;
        }

        public Builder buildContentId(String buildContentId) {
            this.buildContentId = buildContentId;
            return this;
        }

        public Builder temporaryBuild(boolean temporaryBuild) {
            this.temporaryBuild = temporaryBuild;
            return this;
        }

        public Builder submitTime(Date submitTime) {
            this.submitTime = submitTime;
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

        public Builder buildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
            this.buildConfigurationAudited = buildConfigurationAudited;
            return this;
        }

        public Builder buildConfigurationAuditedId(Integer buildConfigurationAuditedId) {
            this.buildConfigurationAuditedId = buildConfigurationAuditedId;
            return this;
        }

        public Builder buildConfigurationAuditedRev(Integer buildConfigurationAuditedRev) {
            this.buildConfigurationAuditedRev = buildConfigurationAuditedRev;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder scmRepoURL(String scmRepoURL) {
            this.scmRepoURL = scmRepoURL;
            return this;
        }

        public Builder scmRevision(String scmRevision) {
            this.scmRevision = scmRevision;
            return this;
        }

        public Builder scmTag(String scmTag) {
            this.scmTag = scmTag;
            return this;
        }

        public Builder buildLog(String buildLog) {
            this.buildLog = buildLog;
            return this;
        }

        public Builder appendLog(String buildLog) {
            this.buildLog += buildLog;
            return this;
        }

        public Builder buildOutputChecksum(String buildOutputChecksum) {
            this.buildOutputChecksum = buildOutputChecksum;
            return this;
        }

        public Builder status(BuildStatus status) {
            this.status = status;
            return this;
        }

        public Builder dependency(Artifact artifact) {
            this.dependencies.add(artifact);
            return this;
        }

        public Builder dependencies(Set<Artifact> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder dependencies(List<Artifact> dependencies) {
            this.dependencies.addAll(dependencies);
            return this;
        }

        @Deprecated
        public Builder buildEnvironment(BuildEnvironment buildEnvironment) {
            this.buildEnvironment = buildEnvironment;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Builder buildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
            this.buildConfigSetRecord = buildConfigSetRecord;
            return this;
        }

        public BuildRecord.Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public BuildRecord.Builder sshCommand(String sshCommand) {
            this.sshCommand = sshCommand;
            return this;
        }

        public BuildRecord.Builder sshPassword(String sshPassword) {
            this.sshPassword = sshPassword;
            return this;
        }

        public BuildRecord.Builder executionRootName(String executionRootName) {
            this.executionRootName = executionRootName;
            return this;
        }

        public BuildRecord.Builder executionRootVersion(String executionRootVersion) {
            this.executionRootVersion = executionRootVersion;
            return this;
        }

        public BuildRecord.Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public BuildRecord.Builder repourLog(String log) {
            this.repourLog = log;
            return this;
        }

        public BuildRecord.Builder dependencyBuildRecordIds(Base32LongID[] dependencyBuildRecordIds) {
            this.dependencyBuildRecordIds = dependencyBuildRecordIds;
            return this;
        }

        public BuildRecord.Builder dependentBuildRecordIds(Base32LongID[] dependentBuildRecordIds) {
            this.dependentBuildRecordIds = dependentBuildRecordIds;
            return this;
        }

        public BuildRecord.Builder noRebuildCause(BuildRecord noRebuildCause) {
            this.noRebuildCause = noRebuildCause;
            return this;
        }

        public BuildRecord.Builder lastUpdateTime(Date lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }
    }

}
