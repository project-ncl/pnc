package org.jboss.pnc.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

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
@Table(name = "project_build_result")
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

    @JoinColumn(name = "project_build_configuration_id")
    @ManyToOne
    @ForeignKey(name = "fk_project_build_result_project_build_configuration")
    private ProjectBuildConfiguration projectBuildConfiguration;

    @ManyToOne
    @ForeignKey(name = "fk_project_build_result_user")
    private User user;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "patches_url")
    private String patchesUrl;

    @Lob
    @Column(name = "build_log")
    private String buildLog;

    @Column(name = "build_status")
    @Enumerated(value = EnumType.STRING)
    private BuildStatus buildStatus;

    @Column(name = "built_artifact")
    @OneToMany(mappedBy = "projectBuildResult")
    private List<Artifact> builtArtifact;

    @Column(name = "dependency")
    @OneToMany(mappedBy = "projectBuildResult")
    private List<Artifact> dependency;

    /**
     * Driver that was used to run the build.
     */
    @Column(name = "build_driver_id")
    private String buildDriverId;

    /**
     * Image that was used to instantiate a build server.
     */
    @JoinColumn(name = "system_image_id")
    @ForeignKey(name = "fk_project_build_result_system_image")
    private SystemImage systemImage;

    @Column(name = "build_collection")
    @ManyToMany(mappedBy = "projectBuildResult")
    private List<BuildCollection> buildCollection;

    /**
     * Instantiates a new project build result.
     */
    public ProjectBuildResult() {
        startTime = Timestamp.from(Instant.now());
        buildCollection = new ArrayList<>();
        dependency = new ArrayList<>();
        builtArtifact = new ArrayList<>();
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
     * @return the buildStatus
     */
    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    /**
     * @param buildStatus the buildStatus to set
     */
    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    /**
     * @return the builtArtifact
     */
    public List<Artifact> getBuiltArtifact() {
        return builtArtifact;
    }

    /**
     * @param builtArtifact the builtArtifact to set
     */
    public void setBuiltArtifact(List<Artifact> builtArtifact) {
        this.builtArtifact = builtArtifact;
    }

    /**
     * @return the dependency
     */
    public List<Artifact> getDependency() {
        return dependency;
    }

    /**
     * @param dependency the dependency to set
     */
    public void setDependency(List<Artifact> dependency) {
        this.dependency = dependency;
    }

    /**
     * @return the buildCollection
     */
    public List<BuildCollection> getBuildCollection() {
        return buildCollection;
    }

    /**
     * @param buildCollection the buildCollection to set
     */
    public void setBuildCollection(List<BuildCollection> buildCollection) {
        this.buildCollection = buildCollection;
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
