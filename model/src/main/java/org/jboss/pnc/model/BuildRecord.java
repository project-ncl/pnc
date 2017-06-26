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

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * <p>
 * This class contains the build result of a project configuration, and contains additional metadata, as the build script, the
 * starting and ending time of a build, the status of the build, the sources url used, the user that triggered the build, plus
 * all the Artifacts that were built and all the Artifacts that were used for the final build. It stores also the buildDriverID
 * that was used to run the build, the system Image where is was run in, and is mapped to a BuildRecordSet, that encapsulates
 * the set of buildRecord that compose a Product
 */
@Entity
public class BuildRecord implements GenericEntity<Integer> {

    private static final long serialVersionUID = -5472083609387609797L;

    public static final String SEQUENCE_NAME = "build_record_id_seq";

    @Id
    private Integer id;

    /**
     * Link to the latest version of the configuration settings for building this project.
     * These settings may have been updated since this record was created, so this
     * can not be used to run an exact rebuild, but it is convenient for reference
     * if a new build of the same project needs to be executed.
     * The join column "buildconfiguration_id" is the same db field used by
     * buildConfigurationAudited, thus this is a read-only field which automatically
     * changes when the buildConfigurationAudited is changed.
     */
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "buildconfiguration_id", insertable = false, updatable = false)
    @ForeignKey(name = "fk_buildrecord_buildconfiguration")
    @Index(name="idx_buildrecord_buildconfiguration")
    private BuildConfiguration latestBuildConfiguration;

    /**
     * Contains the settings that were used at the time the build was executed.
     * Hibernate envers identifies each audited record using the "id" of the
     * original db record along with a revision number.  This can be used to
     * re-run the build with the exact same settings used previously.
     */
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({ @JoinColumn(name = "buildconfiguration_id", referencedColumnName = "id"),
            @JoinColumn(name = "buildconfiguration_rev", referencedColumnName = "rev") })
    @ForeignKey(name = "fk_buildrecord_buildconfiguration_aud")
    @Index(name="idx_buildrecord_buildconfiguration_aud")
    private BuildConfigurationAudited buildConfigurationAudited;

    @Size(max=100)
    private String buildContentId;

    /**
     * The time which the build was submitted to the system.
     */
    @NotNull
    @Column(columnDefinition="timestamp with time zone")
    private Date submitTime;

    /**
     * The time when the build execution started.  Note that it's possible for this to
     * be null in the case of a system error before the build is started.
     */
    @Column(columnDefinition="timestamp with time zone")
    private Date startTime;

    /**
     * The time when the build completed.  Note that it's possible for this to be null
     * if the build never finished.
     */
    @Column(columnDefinition="timestamp with time zone")
    private Date endTime;

    @NotNull
    @ManyToOne
    @ForeignKey(name = "fk_buildrecord_user")
    @Index(name="idx_buildrecord_user")
    private User user;

    /**
     * The scm repository URL used for executing the build.  Note, this can be different
     * than the repository URL contained in the linked build configuration due to pre-build 
     * processing tasks such as repository mirroring and automated build changes.
     */
    @Size(max=255)
    private String scmRepoURL;

    /**
     * The scm revision used for build execution.  Note, this can be different than the
     * revision submitted by the user due to automated build processing steps which modify
     * the sources before executing the build.  This should always be an unmodifiable commit ID
     * and should never be a tag or branch.
     */
    @Size(max=255)
    private String scmRevision;

    @Lob
    @Type(type = "org.hibernate.type.StringType")
    @Basic(fetch = FetchType.LAZY)
    private String buildLog;

    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    @Getter
    @Setter
    @Size(max=150)
    private String sshCommand;

    @Getter
    @Setter
    @Size(max=64)
    private String sshPassword;

    /**
     * This is an identifier of the built project sources.
     * In case of Maven, it is GA of the POM being built.
     * This information comes from Repour/PME and has to be stored in the build record
     * to be used in the release process.
     */
    @Getter
    @Setter
    @Size(max=255)
    private String executionRootName;

    /**
     * See {@link BuildRecord#executionRootName}.
     * Contains corresponding version.
     */
    @Getter
    @Setter
    @Size(max=100)
    private String executionRootVersion;

    /**
     * Artifacts which were produced by this build
     */
    @ManyToMany
    @JoinTable(name = "build_record_built_artifact_map", joinColumns = {
            @JoinColumn(name = "build_record_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "built_artifact_id", referencedColumnName = "id") }, uniqueConstraints = @UniqueConstraint(name = "uk_build_record_id_built_artifact_id", columnNames = {
                            "build_record_id", "built_artifact_id" }) )
    @ForeignKey(name = "fk_build_record_built_artifact_map")
    @Index(name = "idx_build_record_built_artifact_map")
    private Set<Artifact> builtArtifacts;

    /**
     * Artifacts which are required external dependencies of this build
     */
    @ManyToMany
    @JoinTable(name = "build_record_artifact_dependencies_map", joinColumns = {
            @JoinColumn(name = "build_record_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "dependency_artifact_id", referencedColumnName = "id") }, uniqueConstraints = @UniqueConstraint(name = "uk_build_record_id_dependency_artifact_id", columnNames = {
                            "build_record_id", "dependency_artifact_id" }) )
    @ForeignKey(name = "fk_build_record_artifact_dependencies_map")
    @Index(name = "idx_build_record_artifact_dependencies_map")
    private Set<Artifact> dependencies;

    /**
     * Environment configuration (including system image) that was used to instantiate the build host.
     */
    @ManyToOne
    @ForeignKey(name = "fk_buildrecord_buildenvironment")
    @Index(name="idx_buildrecord_buildenvironment")
    private BuildEnvironment buildEnvironment;

    /**
     * The product milestone for which this build was performed.  Even though the artifacts
     * from this build may be included in multiple product milestones/releases, there
     * should only be a single primary product milestone which originally produced this build.
     */
    @ManyToOne
    private ProductMilestone productMilestone;

    /**
     * If this build was executed as part of a set, this will contain the link to the overall results of the set. Otherwise,
     * this field will be null.
     */
    @ManyToOne
    @ForeignKey(name = "fk_buildrecord_buildconfigsetrecord")
    @Index(name="idx_buildrecord_buildconfigsetrecord")
    private BuildConfigSetRecord buildConfigSetRecord;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="build_record_attributes", joinColumns=@JoinColumn(name="build_record_id"))
    @MapKeyColumn(name="key")
    @Column(name="value")
    private Map<String, String> attributes = new HashMap<>();

    @Lob
    @Type(type = "org.hibernate.type.StringType")
    @Basic(fetch = FetchType.LAZY)
    private String repourLog;

    /**
     * Instantiates a new project build result.
     */
    public BuildRecord() {
        dependencies = new HashSet<>();
        builtArtifacts = new HashSet<>();
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
     * The time when the build execution was started.
     * The build task wait time can be determined by the difference
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
     * Get the time when the build finished.
     * The build duration can be determined by the difference
     * between the endTime and the startTime.
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

    public String getRepourLog() {
        return repourLog;
    }

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
     */
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

    public void addBuiltArtifact(Artifact builtArtifact) {
        builtArtifacts.add(builtArtifact);
    }

    /**
     * Sets the built artifacts.
     *
     * @param builtArtifacts the new built artifacts
     */
    public void setBuiltArtifacts(Set<Artifact> builtArtifacts) {
        this.builtArtifacts = builtArtifacts;
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
    public BuildEnvironment getBuildEnvironment() {
        return buildEnvironment;
    }

    /**
     * Sets the build environment.
     *
     * @param buildEnvironment the build environment configuration
     */
    public void setBuildEnvironment(BuildEnvironment buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    /**
     * @return The latest version of the build configuration used to create this build record
     */
    public BuildConfiguration getLatestBuildConfiguration() {
        return latestBuildConfiguration;
    }

    public void setLatestBuildConfiguration(BuildConfiguration latestBuildConfiguration) {
        this.latestBuildConfiguration = latestBuildConfiguration;
    }

    /**
     * @return The audited version of the build configuration used to create this build record
     */
    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public void setBuildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
        this.buildConfigurationAudited = buildConfigurationAudited;
    }

    /**
     * The product milestone for which this build was performed
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
     * @param buildContentId The identifier to use when accessing repository or other content stored via external services.
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
        return "BuildRecord [id=" + id + ", project=" + buildConfigurationAudited.getProject().getName()
                + ", buildConfiguration=" + buildConfigurationAudited + ", status=" + status + "]";
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public String putAttribute(String key, String value) {
        return attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public static class Builder {

        private Integer id;

        private String buildContentId;

        private Date submitTime;

        private Date startTime;

        private Date endTime;

        private BuildConfiguration latestBuildConfiguration;

        private BuildConfigurationAudited buildConfigurationAudited;

        private User user;

        private String scmRepoURL;

        private String scmRevision;

        private String repourLog = "";

        private String buildLog = "";

        private BuildStatus status;

        private Set<Artifact> builtArtifacts;

        private Set<Artifact> dependencies;

        private BuildEnvironment buildEnvironment;

        private ProductMilestone productMilestone;

        private BuildConfigSetRecord buildConfigSetRecord;

        private String sshCommand;

        private String sshPassword;

        private String executionRootName;

        private String executionRootVersion;

        private Map<String, String> attributes = new HashMap<>();

        public Builder() {
            builtArtifacts = new HashSet<>();
            dependencies = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecord build() {
            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setId(id);
            buildRecord.setBuildContentId(buildContentId);
            buildRecord.setSubmitTime(submitTime);
            buildRecord.setStartTime(startTime);
            buildRecord.setEndTime(endTime);
            buildRecord.setLatestBuildConfiguration(latestBuildConfiguration);
            buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
            buildRecord.setUser(user);
            buildRecord.setScmRepoURL(scmRepoURL);
            buildRecord.setScmRevision(scmRevision);
            buildRecord.setRepourLog(repourLog);
            buildRecord.setBuildLog(buildLog);
            buildRecord.setStatus(status);
            buildRecord.setBuildEnvironment(buildEnvironment);
            buildRecord.setProductMilestone(productMilestone);
            buildRecord.setAttributes(attributes);
            buildRecord.setSshCommand(sshCommand);
            buildRecord.setSshPassword(sshPassword);
            buildRecord.setExecutionRootName(executionRootName);
            buildRecord.setExecutionRootVersion(executionRootVersion);

            if (buildConfigSetRecord != null) {
                buildRecord.setBuildConfigSetRecord(buildConfigSetRecord);
            }

            // Set the bi-directional mapping
            for (Artifact artifact : builtArtifacts) {
                artifact.addBuildRecord(buildRecord);
            }
            buildRecord.setBuiltArtifacts(builtArtifacts);

            // Set the bi-directional mapping
            for (Artifact artifact : dependencies) {
                artifact.addDependantBuildRecord(buildRecord);
            }
            buildRecord.setDependencies(dependencies);

            return buildRecord;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder buildContentId(String buildContentId) {
            this.buildContentId = buildContentId;
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

        public Builder latestBuildConfiguration(BuildConfiguration latestBuildConfiguration) {
            this.latestBuildConfiguration = latestBuildConfiguration;
            return this;
        }

        public Builder buildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
            this.buildConfigurationAudited = buildConfigurationAudited;
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

        public Builder buildLog(String buildLog) {
            this.buildLog = buildLog;
            return this;
        }

        public Builder appendLog(String buildLog) {
            this.buildLog += buildLog;
            return this;
        }

        public Builder status(BuildStatus status) {
            this.status = status;
            return this;
        }

        public Builder builtArtifact(Artifact builtArtifact) {
            this.builtArtifacts.add(builtArtifact);
            return this;
        }

        public Builder builtArtifacts(Set<Artifact> builtArtifacts) {
            this.builtArtifacts = builtArtifacts;
            return this;
        }

        public Builder builtArtifacts(List<Artifact> builtArtifacts) {
            this.builtArtifacts.addAll(builtArtifacts);
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
    }

}
