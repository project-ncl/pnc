package org.jboss.pnc.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.persistence.*;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * This class contains the build result of a project configuration, and contains additional metadata, as the build script, the
 * starting and ending time of a build, the status of the build, the sources url used, the user that triggered the build, plus
 * all the Artifacts that were built and all the Artifacts that were used for the final build. It stores also the buildDriverID
 * that was used to run the build, the system Image where is was run in, and is mapped to a BuildCollection, that encapsulates
 * the set of buildResult that compose a Product
 */
@Entity
@Table(name = "build_result")
@NamedQuery(name = "ProjectBuildResult.findAll", query = "SELECT b FROM ProjectBuildResult b")
public class ProjectBuildResult implements Serializable {

    private static final long serialVersionUID = -5472083609387609797L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "build_script")
    private String buildScript;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @ManyToOne
    @Column(name = "project_build_configuration_id")
    private ProjectBuildConfiguration projectBuildConfiguration;

    @ManyToOne
    @Column(name = "user_id")
    private User user;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "patches_url")
    private String patchesUrl;

    @Lob
    @Column(name = "build_log")
    private String buildLog;

    @Enumerated(value = EnumType.STRING)
    private BuildStatus status;

    @OneToMany(mappedBy = "projectBuildResult")
    private List<Artifact> builtArtifacts;

    @OneToMany(mappedBy = "projectBuildResult")
    private List<Artifact> dependencies;

    /**
     * Driver that was used to run the build.
     */
    private String buildDriverId;

    /**
     * Image that was used to instantiate a build server.
     */
    @ManyToOne
    @Column(name = "system_image_id")
    private SystemImage systemImage;

    // bi-directional many-to-many association to BuildCollection
    /** The build collections. */
    @ManyToMany(mappedBy = "projectBuildResult")
    private List<BuildCollection> buildCollections;

    /**
     * Instantiates a new project build result.
     */
    public ProjectBuildResult() {
        startTime = Timestamp.from(Instant.now());
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
     * Gets the source url.
     *
     * @return the source url
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the source url.
     *
     * @param sourceUrl the new source url
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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
     * Gets the builds the collections.
     *
     * @return the builds the collections
     */
    public List<BuildCollection> getBuildCollections() {
        return buildCollections;
    }

    /**
     * Sets the builds the collections.
     *
     * @param buildCollections the new builds the collections
     */
    public void setBuildCollections(List<BuildCollection> buildCollections) {
        this.buildCollections = buildCollections;
    }

    /**
     * @return the projectBuildConfiguration
     */
    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    /**
     * @param projectBuildConfiguration the projectBuildConfiguration to set
     */
    public void setProjectBuildConfiguration(ProjectBuildConfiguration projectBuildConfiguration) {
        this.projectBuildConfiguration = projectBuildConfiguration;
    }

    @Override
    public String toString() {
        return "ProjectBuildResult [id=" + id + ", project=" + projectBuildConfiguration.getProject().getName()
                + ", projectBuildConfiguration=" + projectBuildConfiguration + "]";
    }

}
