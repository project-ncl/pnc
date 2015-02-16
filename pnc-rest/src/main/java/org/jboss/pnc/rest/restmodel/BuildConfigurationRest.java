package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.model.builder.ProjectBuilder;

import javax.xml.bind.annotation.XmlRootElement;

import java.sql.Timestamp;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "Configuration")
public class BuildConfigurationRest {

    private Integer id;

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

    public BuildConfigurationRest() {
    }

    public BuildConfigurationRest(BuildConfiguration buildConfiguration) {
        this.id = buildConfiguration.getId();
        this.name = buildConfiguration.getName();
        this.description = buildConfiguration.getDescription();
        this.buildScript = buildConfiguration.getBuildScript();
        this.scmRepoURL = buildConfiguration.getScmRepoURL();
        this.scmRevision = buildConfiguration.getScmRevision();
        this.patchesUrl = buildConfiguration.getPatchesUrl();
        this.creationTime = buildConfiguration.getCreationTime();
        this.lastModificationTime = buildConfiguration.getLastModificationTime();
        this.repositories = buildConfiguration.getRepositories();
        performIfNotNull(buildConfiguration.getProject() != null, () -> this.projectId = buildConfiguration.getProject()
                .getId());
        performIfNotNull(buildConfiguration.getEnvironment() != null, () -> this.environmentId = buildConfiguration.getEnvironment().getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public BuildConfiguration toBuildConfiguration() {
        BuildConfigurationBuilder builder = BuildConfigurationBuilder.newBuilder();
        builder.name(name);
        builder.description(description);
        builder.buildScript(buildScript);
        builder.scmRepoURL(scmRepoURL);
        builder.scmRevision(scmRevision);
        builder.patchesUrl(patchesUrl);
        builder.creationTime(creationTime);
        builder.lastModificationTime(lastModificationTime);
        builder.repositories(repositories);

        performIfNotNull(projectId != null, () -> builder.project(ProjectBuilder.newBuilder().id(projectId).build()));
        performIfNotNull(environmentId != null, () -> builder.environment(EnvironmentBuilder.emptyEnvironment().id(environmentId).build()));

        return builder.build();
    }
}
