package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

import static org.jboss.pnc.rest.provider.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
public class BuildRecordRest {

    private Integer id;

    private Timestamp startTime;

    private Timestamp endTime;

    private String buildScript;

    private String sourceUrl;

    private String patchesUrl;

    private BuildDriverStatus status;

    private Integer buildConfigurationId;

    private Integer userId;

    private String buildDriverId;

    private Integer systemImageId;

    public BuildRecordRest() {
    }

    public BuildRecordRest(BuildRecord buildRecord) {
        this.id = buildRecord.getId();
        this.buildScript = buildRecord.getBuildScript();
        this.startTime = buildRecord.getStartTime();
        this.endTime = buildRecord.getEndTime();
        performIfNotNull(buildRecord.getBuildConfiguration() != null, () -> buildConfigurationId = buildRecord.getBuildConfiguration().getId());
        performIfNotNull(buildRecord.getUser() != null, () -> userId = buildRecord.getUser().getId());
        performIfNotNull(buildRecord.getSystemImage() != null, () -> systemImageId = buildRecord.getSystemImage().getId());
        this.sourceUrl = buildRecord.getSourceUrl();
        this.patchesUrl = buildRecord.getPatchesUrl();
        this.status = buildRecord.getStatus();
        this.buildDriverId = buildRecord.getBuildDriverId();
    }

    public BuildRecordRest(BuildTask buildTask) {
        this.id = buildTask.getBuildConfiguration().getId();
        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
        this.buildScript = buildConfiguration.getBuildScript();
        this.startTime = buildConfiguration.getCreationTime();
        performIfNotNull(
                buildTask.getBuildConfiguration() != null &&
                        buildConfiguration != null,
                () -> buildConfigurationId = buildConfiguration.getId());
        this.sourceUrl = buildConfiguration.getScmUrl();
        this.patchesUrl = buildConfiguration.getPatchesUrl();
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

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
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
