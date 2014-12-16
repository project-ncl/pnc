package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.ProjectBuildConfigurationBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;

@XmlRootElement(name = "Configuration")
public class ProjectBuildConfigurationRest {

    private Integer id;

    private String identifier;

    private String buildScript;

    private String scmUrl;

    private String patchesUrl;

    private Timestamp creationTime;

    private Timestamp lastModificationTime;

    private String repositories;

    public ProjectBuildConfigurationRest() {
    }

    public ProjectBuildConfigurationRest(ProjectBuildConfiguration projectBuildConfiguration) {
        this.id = projectBuildConfiguration.getId();
        this.identifier = projectBuildConfiguration.getIdentifier();
        this.buildScript = projectBuildConfiguration.getBuildScript();
        this.scmUrl = projectBuildConfiguration.getScmUrl();
        this.patchesUrl = projectBuildConfiguration.getPatchesUrl();
        this.creationTime = projectBuildConfiguration.getCreationTime();
        this.lastModificationTime = projectBuildConfiguration.getLastModificationTime();
        this.repositories = projectBuildConfiguration.getRepositories();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
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

    public ProjectBuildConfiguration getProjectBuildConfiguration(Project project) {
        ProjectBuildConfigurationBuilder builder = ProjectBuildConfigurationBuilder.newBuilder();
        builder.project(project);
        builder.identifier(identifier);
        builder.buildScript(buildScript);
        builder.scmUrl(scmUrl);
        builder.patchesUrl(patchesUrl);
        builder.creationTime(creationTime);
        builder.lastModificationTime(lastModificationTime);
        builder.repositories(repositories);
        return builder.build();
    }
}
