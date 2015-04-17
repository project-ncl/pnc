package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildConfigurationAudited;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildConfigurationAudited")
public class BuildConfigurationAuditedRest {

    private Integer id;

    private Integer rev;

    private String name;

    private String description;

    private String buildScript;

    private String scmRepoURL;

    private String scmRevision;

    private String patchesUrl;

    private Timestamp creationTime;

    private Timestamp lastModificationTime;

    private String repositories;

    private Integer projectId;

    private Integer environmentId;

    public BuildConfigurationAuditedRest() {
    }

    public BuildConfigurationAuditedRest(BuildConfigurationAudited buildConfigurationAudited) {
        this.id = buildConfigurationAudited.getId();
        this.rev = buildConfigurationAudited.getRev();
        this.name = buildConfigurationAudited.getName();
        this.description = buildConfigurationAudited.getDescription();
        this.buildScript = buildConfigurationAudited.getBuildScript();
        this.scmRepoURL = buildConfigurationAudited.getScmRepoURL();
        this.scmRevision = buildConfigurationAudited.getScmRevision();
        this.patchesUrl = buildConfigurationAudited.getPatchesUrl();
        performIfNotNull(buildConfigurationAudited.getProject() != null, () -> this.projectId = buildConfigurationAudited.getProject()
                .getId());
        performIfNotNull(buildConfigurationAudited.getEnvironment() != null, () -> this.environmentId = buildConfigurationAudited.getEnvironment().getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
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

    public String getPatchesUrl() {
        return patchesUrl;
    }

    public void setPatchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Timestamp lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public String getRepositories() {
        return repositories;
    }

    public void setRepositories(String repositories) {
        this.repositories = repositories;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

}
