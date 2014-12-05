/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model.builder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;

/**
 * @author avibelli
 *
 */
public class ProjectBuildConfigurationBuilder {

    private Integer id;

    private String identifier;

    private String buildScript;

    private String scmUrl;

    private String patchesUrl;

    private ProductVersion productVersion;

    private Project project;

    private Environment environment;

    private ProjectBuildConfiguration parent;

    private Set<ProjectBuildConfiguration> dependencies;

    private Timestamp creationTime;

    private Timestamp lastModificationTime;

    private String repositories;

    private ProjectBuildConfigurationBuilder() {
        dependencies = new HashSet<>();
        creationTime = Timestamp.from(Instant.now());
    }

    public static ProjectBuildConfigurationBuilder newBuilder() {
        return new ProjectBuildConfigurationBuilder();
    }

    public ProjectBuildConfiguration build() {

        ProjectBuildConfiguration projectBuildConfiguration = new ProjectBuildConfiguration();
        projectBuildConfiguration.setId(id);
        projectBuildConfiguration.setIdentifier(identifier);
        projectBuildConfiguration.setBuildScript(buildScript);
        projectBuildConfiguration.setScmUrl(scmUrl);
        projectBuildConfiguration.setPatchesUrl(patchesUrl);
        projectBuildConfiguration.setProductVersion(productVersion);
        projectBuildConfiguration.setProject(project);
        projectBuildConfiguration.setEnvironment(environment);
        projectBuildConfiguration.setCreationTime(creationTime);
        projectBuildConfiguration.setLastModificationTime(lastModificationTime);
        projectBuildConfiguration.setRepositories(repositories);

        // Set the bi-directional mapping
        for (ProjectBuildConfiguration dependency : dependencies) {
            dependency.setParent(projectBuildConfiguration);
        }
        projectBuildConfiguration.setDependencies(dependencies);

        return projectBuildConfiguration;
    }

    public ProjectBuildConfigurationBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProjectBuildConfigurationBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ProjectBuildConfigurationBuilder buildScript(String buildScript) {
        this.buildScript = buildScript;
        return this;
    }

    public ProjectBuildConfigurationBuilder scmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
        return this;
    }

    public ProjectBuildConfigurationBuilder patchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
        return this;
    }

    public ProjectBuildConfigurationBuilder productVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public ProjectBuildConfigurationBuilder project(Project project) {
        this.project = project;
        return this;
    }

    public ProjectBuildConfigurationBuilder environment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public ProjectBuildConfigurationBuilder dependency(ProjectBuildConfiguration dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    public ProjectBuildConfigurationBuilder dependencies(Set<ProjectBuildConfiguration> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public ProjectBuildConfigurationBuilder creationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public ProjectBuildConfigurationBuilder lastModificationTime(Timestamp lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
        return this;
    }

    public ProjectBuildConfigurationBuilder repositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public String getPatchesUrl() {
        return patchesUrl;
    }

    public ProductVersion getProductVersion() {
        return productVersion;
    }

    public Project getProject() {
        return project;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ProjectBuildConfiguration getParent() {
        return parent;
    }

    public Set<ProjectBuildConfiguration> getDependencies() {
        return dependencies;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public Timestamp getLastModificationTime() {
        return lastModificationTime;
    }

    public String getRepositories() {
        return repositories;
    }

}
