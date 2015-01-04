package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.core.builder.SubmittedBuild;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

import static org.jboss.pnc.rest.provider.Utility.performIfNotNull;

@XmlRootElement(name = "BuildResult")
public class BuildResultRest {

    private Integer id;

    private Timestamp startTime;

    private Timestamp endTime;

    private String buildScript;

    private String sourceUrl;

    private String patchesUrl;

    private BuildDriverStatus status;

    private Integer projectBuildConfigurationId;

    private Integer userId;

    private String buildDriverId;

    private Integer systemImageId;

    public BuildResultRest() {
    }

    public BuildResultRest(ProjectBuildResult buildResult) {
        this.id = buildResult.getId();
        this.buildScript = buildResult.getBuildScript();
        this.startTime = buildResult.getStartTime();
        this.endTime = buildResult.getEndTime();
        performIfNotNull(buildResult.getProjectBuildConfiguration() != null, () -> projectBuildConfigurationId = buildResult.getProjectBuildConfiguration().getId());
        performIfNotNull(buildResult.getUser() != null, () -> userId = buildResult.getUser().getId());
        performIfNotNull(buildResult.getSystemImage() != null, () -> systemImageId = buildResult.getSystemImage().getId());
        this.sourceUrl = buildResult.getSourceUrl();
        this.patchesUrl = buildResult.getPatchesUrl();
        this.status = buildResult.getStatus();
        this.buildDriverId = buildResult.getBuildDriverId();
    }

    public BuildResultRest(SubmittedBuild submittedBuild) {
        this.id = submittedBuild.getProjectBuildConfiguration().getId();
        ProjectBuildConfiguration projectBuildConfiguration = submittedBuild.getProjectBuildConfiguration();
        this.buildScript = projectBuildConfiguration.getBuildScript();
        this.startTime = projectBuildConfiguration.getCreationTime();
        performIfNotNull(
                submittedBuild.getProjectBuildConfiguration() != null &&
                        projectBuildConfiguration != null,
                () -> projectBuildConfigurationId = projectBuildConfiguration.getId());
        this.sourceUrl = projectBuildConfiguration.getScmUrl();
        this.patchesUrl = projectBuildConfiguration.getPatchesUrl();
        this.status = BuildDriverStatus.BUILDING;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getPatchesUrl() {
        return patchesUrl;
    }

    public void setPatchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
    }

    public BuildDriverStatus getStatus() {
        return status;
    }

    public void setStatus(BuildDriverStatus status) {
        this.status = status;
    }

    public Integer getProjectBuildConfigurationId() {
        return projectBuildConfigurationId;
    }

    public void setProjectBuildConfigurationId(Integer projectBuildConfigurationId) {
        this.projectBuildConfigurationId = projectBuildConfigurationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBuildDriverId() {
        return buildDriverId;
    }

    public void setBuildDriverId(String buildDriverId) {
        this.buildDriverId = buildDriverId;
    }

    public Integer getSystemImageId() {
        return systemImageId;
    }

    public void setSystemImageId(Integer systemImageId) {
        this.systemImageId = systemImageId;
    }

}
