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
package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;

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

    private BuildStatus buildStatus;

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
        this.buildStatus = buildConfiguration.getBuildStatus();
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

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
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

    public BuildConfiguration toBuildConfiguration(BuildConfiguration buildConfiguration) {
        BuildConfiguration.Builder builder = BuildConfiguration.Builder.newBuilder();
        builder.id(id);
        builder.name(name);
        builder.description(description);
        builder.buildScript(buildScript);
        builder.scmRepoURL(scmRepoURL);
        builder.scmRevision(scmRevision);
        builder.patchesUrl(patchesUrl);
        builder.creationTime(creationTime);
        builder.lastModificationTime(lastModificationTime);
        builder.buildStatus(buildStatus);
        builder.repositories(repositories);

        performIfNotNull(projectId != null, () -> builder.project(Project.Builder.newBuilder().id(projectId).build()));
        performIfNotNull(environmentId != null, () -> builder.environment(Environment.Builder.emptyEnvironment().id(environmentId).build()));

        overrideWithDataFromOriginalConfiguration(buildConfiguration, builder);
        return builder.build();
    }

    private void overrideWithDataFromOriginalConfiguration(BuildConfiguration buildConfiguration, BuildConfiguration.Builder builder) {
        performIfNotNull(buildConfiguration != null, () -> {
            builder.lastModificationTime(buildConfiguration.getLastModificationTime());
            builder.creationTime(buildConfiguration.getCreationTime());
        });
    }
}
