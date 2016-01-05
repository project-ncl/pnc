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

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "Configuration")
public class BuildConfigurationRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @NotNull(groups = WhenCreatingNew.class)
    @Pattern(regexp = "^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$", groups = { WhenCreatingNew.class, WhenUpdating.class })
    private String name;

    private String description;

    private String buildScript;

    private String scmRepoURL;

    private String scmRevision;

    private String scmMirrorRepoURL;

    private String scmMirrorRevision;

    private Date creationTime;

    private Date lastModificationTime;

    @ApiModelProperty(dataType = "string")
    private BuildStatus buildStatus;

    private String repositories;

    @NotNull(groups = WhenCreatingNew.class)
    private ProjectRest project;

    private BuildEnvironmentRest environment;

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
        this.scmMirrorRepoURL = buildConfiguration.getScmMirrorRepoURL();
        this.scmMirrorRevision = buildConfiguration.getScmMirrorRevision();
        this.creationTime = buildConfiguration.getCreationTime();
        this.lastModificationTime = buildConfiguration.getLastModificationTime();
        this.buildStatus = buildConfiguration.getBuildStatus();
        this.repositories = buildConfiguration.getRepositories();
        performIfNotNull(buildConfiguration.getProject(),
                () -> this.project = new ProjectRest(buildConfiguration.getProject()));
        performIfNotNull(buildConfiguration.getBuildEnvironment(),
                () -> this.environment = new BuildEnvironmentRest(buildConfiguration.getBuildEnvironment()));
        this.dependencyIds = nullableStreamOf(buildConfiguration.getDependencies())
                .map(dependencyConfig -> dependencyConfig.getId()).collect(Collectors.toSet());
        this.productVersionIds = nullableStreamOf(buildConfiguration.getProductVersions())
                .map(productVersion -> productVersion.getId()).collect(Collectors.toSet());
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

    public String getScmMirrorRepoURL() {
        return scmMirrorRepoURL;
    }

    public void setScmMirrorRepoURL(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    public String getScmMirrorRevision() {
        return scmMirrorRevision;
    }

    public void setScmMirrorRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
    }

    @Deprecated
    public String getInternalScm() {
        return scmMirrorRepoURL;
    }

    @Deprecated
    public void setInternalScm(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    @Deprecated
    public String getInternalScmRevision() {
        return scmMirrorRevision;
    }

    @Deprecated
    public void setInternalScmRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
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

    public ProjectRest getProject() {
        return project;
    }

    public void setProject(ProjectRest project) {
        this.project = project;
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

    public BuildEnvironmentRest getEnvironment() {
        return environment;
    }

    public void setEnvironment(BuildEnvironmentRest environment) {
        this.environment = environment;
    }

    public BuildConfiguration toBuildConfiguration(BuildConfiguration buildConfiguration) {
        BuildConfiguration.Builder builder = BuildConfiguration.Builder.newBuilder();
        builder.id(id);
        builder.name(name);
        builder.description(description);
        builder.buildScript(buildScript);
        builder.scmRepoURL(scmRepoURL);
        builder.scmRevision(scmRevision);
        builder.scmMirrorRepoURL(scmMirrorRepoURL);
        builder.scmMirrorRevision(scmMirrorRevision);
        builder.creationTime(creationTime);
        builder.lastModificationTime(lastModificationTime);
        builder.buildStatus(buildStatus);
        builder.repositories(repositories);

        performIfNotNull(project, () -> builder.project(project.toProject()));
        performIfNotNull(environment, () -> builder.buildEnvironment(environment.toBuildSystemImage()));

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

    private void overrideWithDataFromOriginalConfiguration(BuildConfiguration buildConfiguration,
            BuildConfiguration.Builder builder) {
        performIfNotNull(buildConfiguration, () -> {
            builder.lastModificationTime(buildConfiguration.getLastModificationTime());
            builder.creationTime(buildConfiguration.getCreationTime());
        });
    }
}
