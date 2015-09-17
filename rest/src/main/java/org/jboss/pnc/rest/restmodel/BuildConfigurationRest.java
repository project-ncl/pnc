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

import io.swagger.annotations.ApiModelProperty;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "Configuration")
public class BuildConfigurationRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @NotNull(groups = WhenCreatingNew.class)
    private String name;

    private String description;

    private String buildScript;

    private String scmRepoURL;

    private String scmRevision;

    private Date creationTime;

    private Date lastModificationTime;

    @ApiModelProperty(dataType = "string")
    private BuildStatus buildStatus;

    private String repositories;

    @NotNull(groups = WhenCreatingNew.class)
    private Integer projectId;

    private Integer environmentId;

    private Set<Integer> dependencyIds;

    private Set<Integer> productVersionIds;

    public BuildConfigurationRest() {
    }

    public BuildConfigurationRest(BuildConfiguration buildConfiguration) {
        this.id = buildConfiguration.getId();
        this.name = buildConfiguration.getName();
        this.description = buildConfiguration.getDescription();
        this.buildScript = buildConfiguration.getBuildScript();
        this.scmRepoURL = buildConfiguration.getScmRepoURL();
        this.scmRevision = buildConfiguration.getScmRevision();
        this.creationTime = buildConfiguration.getCreationTime();
        this.lastModificationTime = buildConfiguration.getLastModificationTime();
        this.buildStatus = buildConfiguration.getBuildStatus();
        this.repositories = buildConfiguration.getRepositories();
        performIfNotNull(buildConfiguration.getProject(), () -> this.projectId = buildConfiguration.getProject()
                .getId());
        performIfNotNull(buildConfiguration.getEnvironment(), () -> this.environmentId = buildConfiguration.getEnvironment().getId());
        this.dependencyIds = nullableStreamOf(buildConfiguration.getDependencies()).map(dependencyConfig -> dependencyConfig.getId())
                .collect(Collectors.toSet());
        this.productVersionIds = nullableStreamOf(buildConfiguration.getProductVersions()).map(productVersion -> productVersion.getId())
                .collect(Collectors.toSet());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Date lastModificationTime) {
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

    public Set<Integer> getProductVersionIds() {
        return productVersionIds;
    }

    public void setProductVersionIds(Set<Integer> productVersionIds) {
        this.productVersionIds = productVersionIds;
    }

    public boolean addProductVersion(Integer productVersionId) {
        return this.productVersionIds.add(productVersionId);
    }

    public boolean removeProductVersion(Integer productVersionId) {
        return this.productVersionIds.remove(productVersionId);
    }

    public Set<Integer> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(Set<Integer> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }

    public boolean addDependency(Integer dependencyId) {
        return dependencyIds.add(dependencyId);
    }

    public boolean removeDependency(Integer dependencyId) {
        return dependencyIds.remove(dependencyId);
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
        builder.creationTime(creationTime);
        builder.lastModificationTime(lastModificationTime);
        builder.buildStatus(buildStatus);
        builder.repositories(repositories);

        performIfNotNull(projectId, () -> builder.project(Project.Builder.newBuilder().id(projectId).build()));
        performIfNotNull(environmentId, () -> builder.environment(Environment.Builder.emptyEnvironment().id(environmentId).build()));

        nullableStreamOf(dependencyIds).forEach(dependencyId -> {
            BuildConfiguration.Builder buildConfigurationBuilder = BuildConfiguration.Builder.newBuilder().id(dependencyId);
            builder.dependency(buildConfigurationBuilder.build());
        });
        nullableStreamOf(productVersionIds).forEach(productVersionId -> {
            ProductVersion.Builder productVersionBuilder = ProductVersion.Builder.newBuilder().id(productVersionId);
            builder.productVersion(productVersionBuilder.build());
        });

        overrideWithDataFromOriginalConfiguration(buildConfiguration, builder);
        return builder.build();
    }

    private void overrideWithDataFromOriginalConfiguration(BuildConfiguration buildConfiguration, BuildConfiguration.Builder builder) {
        performIfNotNull(buildConfiguration, () -> {
            builder.lastModificationTime(buildConfiguration.getLastModificationTime());
            builder.creationTime(buildConfiguration.getCreationTime());
        });
    }
}
