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

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * @author avibelli
 *
 */
public class BuildConfigurationBuilder {

    private Integer id;

    private String name;

    private String buildScript;

    private String scmRepoURL;

    private String scmRevision;

    private String patchesUrl;

    private String description;

    private ProductVersion productVersion;

    private Project project;

    private Environment environment;

    private BuildConfiguration parent;

    private Set<BuildConfiguration> dependencies;

    private Timestamp creationTime;

    private Timestamp lastModificationTime;

    private String repositories;

    private BuildConfigurationBuilder() {
        dependencies = new HashSet<>();
        creationTime = Timestamp.from(Instant.now());
    }

    public static BuildConfigurationBuilder newBuilder() {
        return new BuildConfigurationBuilder();
    }

    public BuildConfiguration build() {

        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(id);
        buildConfiguration.setName(name);
        buildConfiguration.setBuildScript(buildScript);
        buildConfiguration.setScmRepoURL(scmRepoURL);
        buildConfiguration.setScmRevision(scmRevision);
        buildConfiguration.setPatchesUrl(patchesUrl);
        buildConfiguration.setDescription(description);
        buildConfiguration.setProductVersion(productVersion);

        // Set the bi-directional mapping
        if (project != null) {
            project.addBuildConfiguration(buildConfiguration);
        }
        buildConfiguration.setProject(project);

        buildConfiguration.setEnvironment(environment);
        buildConfiguration.setCreationTime(creationTime);
        buildConfiguration.setLastModificationTime(lastModificationTime);
        buildConfiguration.setRepositories(repositories);

        // Set the bi-directional mapping
        for (BuildConfiguration dependency : dependencies) {
            dependency.setParent(buildConfiguration);
        }
        buildConfiguration.setDependencies(dependencies);

        return buildConfiguration;
    }

    public BuildConfigurationBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public BuildConfigurationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BuildConfigurationBuilder buildScript(String buildScript) {
        this.buildScript = buildScript;
        return this;
    }

    public BuildConfigurationBuilder scmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
        return this;
    }

    public BuildConfigurationBuilder scmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
        return this;
    }

    public BuildConfigurationBuilder patchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
        return this;
    }

    public BuildConfigurationBuilder description(String description) {
        this.description = description;
        return this;
    }

    public BuildConfigurationBuilder productVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    public BuildConfigurationBuilder project(Project project) {
        this.project = project;
        return this;
    }

    public BuildConfigurationBuilder environment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public BuildConfigurationBuilder dependency(BuildConfiguration dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    public BuildConfigurationBuilder dependencies(Set<BuildConfiguration> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    /**
     * Sets create time and ignores Null values (since they may affect the entity consistency).
     */
    public BuildConfigurationBuilder creationTime(Timestamp creationTime) {
        if (creationTime != null) {
            this.creationTime = creationTime;
        }
        return this;
    }

    public BuildConfigurationBuilder lastModificationTime(Timestamp lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
        return this;
    }

    public BuildConfigurationBuilder repositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public String getPatchesUrl() {
        return patchesUrl;
    }

    public String getDescription() {
        return description;
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

    public BuildConfiguration getParent() {
        return parent;
    }

    public Set<BuildConfiguration> getDependencies() {
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
