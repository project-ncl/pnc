package org.jboss.pnc.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;

import java.io.Serializable;
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
public class BuildRecord implements Serializable {

    private static final long serialVersionUID = -5472083609387609797L;

    public static final String DEFAULT_SORTING_FIELD = "id";

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    private BuildConfiguration buildConfiguration;

    private String buildScript;

    private String name;

    private String description;

    private String scmRepoURL;

    private String scmRevision;

    private String patchesUrl;

    private Timestamp startTime;

    private Timestamp endTime;

    @ManyToOne
    private User user;

    @Lob
    private String buildLog;

    @Enumerated(value = EnumType.STRING)
    private BuildDriverStatus status;

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
     * Gets the builds the script.
     *
     * @return the builds the script
     */
    public String getBuildScript() {
        return buildScript;
    }

    /**
     * Sets the builds the script.
     *
     * @param buildScript the new builds the script
     */
    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
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
     * @return the scmRepoURL
     */
    public String getScmRepoURL() {
        return scmRepoURL;
    }

    /**
     * @param scmRepoURL the scmRepoURL to set
     */
    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    /**
     * @return the scmRevision
     */
    public String getScmRevision() {
        return scmRevision;
    }

    /**
     * @param scmRevision the scmRevision to set
     */
    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    /**
     * Gets the patches url.
     *
     * @return the patches url
     */
    public String getPatchesUrl() {
        return patchesUrl;
    }

    /**
     * Sets the patches url.
     *
     * @param patchesUrl the new patches url
     */
    public void setPatchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
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
    public BuildDriverStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(BuildDriverStatus status) {
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
     * @return the buildConfiguration
     */
    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    /**
     * @param buildConfiguration the buildConfiguration to set
     */
    public void setBuildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "BuildRecord [id=" + id + ", project=" + buildConfiguration.getProject().getName() + ", buildConfiguration="
                + buildConfiguration + "]";
    }

    public static class Builder {

        private Integer id;

        private String buildScript;

        private String name;

        private String description;

        private String scmRepoURL;

        private String scmRevision;

        private String patchesUrl;

        private Timestamp startTime;

        private Timestamp endTime;

        private BuildConfiguration buildConfiguration;

        private User user;

        private String buildLog;

        private BuildDriverStatus status;

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
            buildRecord.setBuildScript(buildScript);
            buildRecord.setName(name);
            buildRecord.setDescription(description);
            buildRecord.setStartTime(startTime);
            buildRecord.setEndTime(endTime);
            buildRecord.setBuildConfiguration(buildConfiguration);
            buildRecord.setUser(user);
            buildRecord.setScmRepoURL(scmRepoURL);
            buildRecord.setScmRevision(scmRevision);
            buildRecord.setPatchesUrl(patchesUrl);
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

        public Builder buildScript(String buildScript) {
            this.buildScript = buildScript;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder buildConfiguration(BuildConfiguration buildConfiguration) {
            this.buildConfiguration = buildConfiguration;
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

        public Builder patchesUrl(String patchesUrl) {
            this.patchesUrl = patchesUrl;
            return this;
        }

        public Builder buildLog(String buildLog) {
            this.buildLog = buildLog;
            return this;
        }

        public Builder status(BuildDriverStatus status) {
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
