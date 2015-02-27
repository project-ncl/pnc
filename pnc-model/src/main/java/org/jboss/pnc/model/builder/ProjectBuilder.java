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

import java.util.HashSet;
import java.util.Set;

import org.jboss.pnc.model.License;
import org.jboss.pnc.model.ProductVersionProject;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;

/**
 * @author avibelli
 *
 */
public class ProjectBuilder {

    private Integer id;

    private String name;

    private String description;

    private String issueTrackerUrl;

    private String projectUrl;

    private License license;

    private Set<ProductVersionProject> productVersionProjects;

    private Set<BuildConfiguration> buildConfigurations;

    private ProjectBuilder() {
        productVersionProjects = new HashSet<>();
        buildConfigurations = new HashSet<>();
    }

    public static ProjectBuilder newBuilder() {
        return new ProjectBuilder();
    }

    public Project build() {

        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription(description);
        project.setIssueTrackerUrl(issueTrackerUrl);
        project.setProjectUrl(projectUrl);
        project.setLicense(license);

        // Set the bi-directional mapping
        for (ProductVersionProject productVersionProject : productVersionProjects) {
            productVersionProject.setProject(project);
        }
        project.setProductVersionProjects(productVersionProjects);

        // Set the bi-directional mapping
        for (BuildConfiguration buildConfiguration : buildConfigurations) {
            buildConfiguration.setProject(project);
        }
        project.setBuildConfigurations(buildConfigurations);

        return project;
    }

    public ProjectBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProjectBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProjectBuilder issueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
        return this;
    }

    public ProjectBuilder projectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
        return this;
    }

    public ProjectBuilder license(License license) {
        this.license = license;
        return this;
    }

    public ProjectBuilder productVersionProject(ProductVersionProject productVersionProject) {
        this.productVersionProjects.add(productVersionProject);
        return this;
    }

    public ProjectBuilder buildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfigurations.add(buildConfiguration);
        return this;
    }

    public ProjectBuilder productVersionProjects(Set<ProductVersionProject> productVersionProjects) {
        this.productVersionProjects = productVersionProjects;
        return this;
    }

    public ProjectBuilder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
        return this;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public License getLicense() {
        return license;
    }

    public Set<ProductVersionProject> getProductVersionProjects() {
        return productVersionProjects;
    }

    public Set<BuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

}
