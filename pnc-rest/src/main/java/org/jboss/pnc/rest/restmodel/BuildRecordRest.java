package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecord")
public class BuildRecordRest {

    private Integer id;

    private Timestamp startTime;

    private Timestamp endTime;

    private BuildStatus status;

    private Integer buildConfigurationId;

    private Integer buildConfigurationRev;

    private Integer userId;

    private String buildDriverId;

    private Integer systemImageId;

    public BuildRecordRest() {
    }

    public BuildRecordRest(BuildRecord buildRecord) {
        this.id = buildRecord.getId();
        this.startTime = buildRecord.getStartTime();
        this.endTime = buildRecord.getEndTime();
        performIfNotNull(buildRecord.getBuildConfigurationAudited() != null, () -> buildConfigurationId = buildRecord
                .getBuildConfigurationAudited().getId());
        performIfNotNull(buildRecord.getBuildConfigurationAudited() != null, () -> buildConfigurationRev = buildRecord
                .getBuildConfigurationAudited().getRev());
        performIfNotNull(buildRecord.getUser() != null, () -> userId = buildRecord.getUser().getId());
        performIfNotNull(buildRecord.getSystemImage() != null, () -> systemImageId = buildRecord.getSystemImage().getId());
        this.status = buildRecord.getStatus();
        this.buildDriverId = buildRecord.getBuildDriverId();
    }

    public BuildRecordRest(BuildTask buildTask) {
        this.id = buildTask.getBuildConfiguration().getId();
        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
        this.startTime = buildConfiguration.getCreationTime();
        performIfNotNull(buildTask.getBuildConfiguration() != null && buildConfiguration != null,
                () -> buildConfigurationId = buildConfiguration.getId());
        this.status = BuildStatus.BUILDING;
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

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public Integer getBuildConfigurationId() {
        return buildConfigurationId;
    }

    public void setBuildConfigurationId(Integer buildConfigurationId) {
        this.buildConfigurationId = buildConfigurationId;
    }

    public Integer getBuildConfigurationRev() {
        return buildConfigurationRev;
    }

    public void setBuildConfigurationRev(Integer buildConfigurationRev) {
        this.buildConfigurationRev = buildConfigurationRev;
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
