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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Id
    @SequenceGenerator(name="build_record_id_seq", sequenceName="build_record_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="build_record_id_seq")
    private Integer id;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "buildconfiguration_id", insertable=false, updatable=false)
    private BuildConfiguration latestBuildConfiguration;  //TODO do we need latest and audited build configuration

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumns({
        @JoinColumn(name="buildconfiguration_id", referencedColumnName= "id"),
        @JoinColumn(name="buildconfiguration_rev", referencedColumnName= "rev")
    })
    private BuildConfigurationAudited buildConfigurationAudited;

    private String buildContentId;

    @NotNull
    private Timestamp startTime;

    @NotNull
    private Timestamp endTime;

    //@NotNull //TODO uncomment
    @ManyToOne
    private User user;

    @Lob
    private String buildLog;

    @Enumerated(value = EnumType.STRING)
    private BuildStatus status;

    @OneToMany(mappedBy = "buildRecord", cascade = CascadeType.ALL)
    private List<Artifact> builtArtifacts;

    @OneToMany(mappedBy = "buildRecord", cascade = CascadeType.ALL)
    private List<Artifact> dependencies;

    /**
     * Driver that was used to run the build.
     */
    private String buildDriverId;

    /**
     * Image that was used to instantiate a build server.
     */
    @ManyToOne
    private SystemImage systemImage;

    // bi-directional many-to-many association to buildRecordSet

    /**
     * The build collections.
     */
    @ManyToMany(mappedBy = "buildRecord")
    private List<BuildRecordSet> buildRecordSets;

    /**
     * Instantiates a new project build result.
     */
    public BuildRecord() {
        startTime = Timestamp.from(Instant.now());
        buildRecordSets = new ArrayList<>();
        dependencies = new ArrayList<>();
        builtArtifacts = new ArrayList<>();
    }

    @PreRemove
    private void removeBuildRecordFromSets() {
        for (BuildRecordSet brs : buildRecordSets) {
            brs.getBuildRecord().remove(this);
        }
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
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public Timestamp getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the new start time
     */
    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public Timestamp getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     *
     * @param endTime the new end time
     */
    public void setEndTime(Timestamp endTime) {
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
    public List<Artifact> getBuiltArtifacts() {
        return builtArtifacts;
    }

    /**
     * Sets the built artifacts.
     *
     * @param builtArtifacts the new built artifacts
     */
    public void setBuiltArtifacts(List<Artifact> builtArtifacts) {
        this.builtArtifacts = builtArtifacts;
    }

    /**
     * Gets the dependencies.
     *
     * @return the dependencies
     */
    public List<Artifact> getDependencies() {
        return dependencies;
    }

    /**
     * Sets the dependencies.
     *
     * @param dependencies the new dependencies
     */
    public void setDependencies(List<Artifact> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Gets the builds the driver id.
     *
     * @return the builds the driver id
     */
    public String getBuildDriverId() {
        return buildDriverId;
    }

    /**
     * Sets the builds the driver id.
     *
     * @param buildDriverId the new builds the driver id
     */
    public void setBuildDriverId(String buildDriverId) {
        this.buildDriverId = buildDriverId;
    }

    /**
     * Gets the system image.
     *
     * @return the system image
     */
    public SystemImage getSystemImage() {
        return systemImage;
    }

    /**
     * Sets the system image.
     *
     * @param systemImage the new system image
     */
    public void setSystemImage(SystemImage systemImage) {
        this.systemImage = systemImage;
    }

    /**
     * @return The latest version of the build configuration 
     * used to create this build record
     */
    public BuildConfiguration getLatestBuildConfiguration() {
        return latestBuildConfiguration;
    }

    public void setLatestBuildConfiguration(BuildConfiguration latestBuildConfiguration) {
        this.latestBuildConfiguration = latestBuildConfiguration;
    }

    /**
     * @return The audited version of the build configuration 
     * used to create this build record
     */
    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public void setBuildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
        this.buildConfigurationAudited = buildConfigurationAudited;
    }

    /**
     * @return the buildRecordSets
     */
    public List<BuildRecordSet> getBuildRecordSets() {
        return buildRecordSets;
    }

    /**
     * @param buildRecordSets the buildRecordSets to set
     */
    public void setBuildRecordSets(List<BuildRecordSet> buildRecordSets) {
        this.buildRecordSets = buildRecordSets;
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

    @Override
    public String toString() {
        return "BuildRecord [id=" + id + ", project=" + buildConfigurationAudited.getProject().getName() + ", buildConfiguration="
                + buildConfigurationAudited + "]";
    }

    public static class Builder {

        private Integer id;

        private String buildContentId;

        private Timestamp startTime;

        private Timestamp endTime;

        private BuildConfiguration latestBuildConfiguration;

        private BuildConfigurationAudited buildConfigurationAudited;

        private User user;

        private String buildLog;

        private BuildStatus status;

        private List<Artifact> builtArtifacts;

        private List<Artifact> dependencies;

        private String buildDriverId;

        private SystemImage systemImage;

        private List<BuildRecordSet> buildRecordSets;

        public Builder() {
            startTime = Timestamp.from(Instant.now());
            buildRecordSets = new ArrayList<>();
            dependencies = new ArrayList<>();
            builtArtifacts = new ArrayList<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecord build() {
            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setId(id);
            buildRecord.setBuildContentId(buildContentId);
            buildRecord.setStartTime(startTime);
            buildRecord.setEndTime(endTime);
            if (latestBuildConfiguration != null) {
                latestBuildConfiguration.addBuildRecord(buildRecord);
                buildRecord.setLatestBuildConfiguration(latestBuildConfiguration);
            }
            buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
            buildRecord.setUser(user);
            buildRecord.setBuildLog(buildLog);
            buildRecord.setStatus(status);
            buildRecord.setBuildDriverId(buildDriverId);
            buildRecord.setSystemImage(systemImage);

            // Set the bi-directional mapping
            for (Artifact artifact : builtArtifacts) {
                artifact.setBuildRecord(buildRecord);
            }
            buildRecord.setBuiltArtifacts(builtArtifacts);

            // Set the bi-directional mapping
            for (Artifact artifact : dependencies) {
                artifact.setBuildRecord(buildRecord);
            }
            buildRecord.setDependencies(dependencies);

            // Set the bi-directional mapping
            for (BuildRecordSet buildRecordSet : buildRecordSets) {
                buildRecordSet.getBuildRecord().add(buildRecord);
            }
            buildRecord.setBuildRecordSets(buildRecordSets);

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

        public Builder startTime(Timestamp startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Timestamp endTime) {
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

        public Builder buildLog(String buildLog) {
            this.buildLog = buildLog;
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

        public Builder builtArtifacts(List<Artifact> builtArtifacts) {
            this.builtArtifacts = builtArtifacts;
            return this;
        }

        public Builder dependency(Artifact builtArtifact) {
            this.dependencies.add(builtArtifact);
            return this;
        }

        public Builder dependencies(List<Artifact> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder buildDriverId(String buildDriverId) {
            this.buildDriverId = buildDriverId;
            return this;
        }

        public Builder systemImage(SystemImage systemImage) {
            this.systemImage = systemImage;
            return this;
        }

        public Builder buildRecordSets(List<BuildRecordSet> buildRecordSets) {
            this.buildRecordSets = buildRecordSets;
            return this;
        }

    }

}
