/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "Project")
public class ProjectRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String name;

    private String description;

    private String issueTrackerUrl;

    private String projectUrl;

    private List<Integer> configurationIds;

    private Integer licenseId;

    /**
     * Instantiates a new project rest.
     */
    public ProjectRest() {
    }

    /**
     * Instantiates a new project rest.
     *
     * @param project the project
     */
    public ProjectRest(Project project) {
        this.id = project.getId();
        this.description = project.getDescription();
        this.name = project.getName();
        this.issueTrackerUrl = project.getIssueTrackerUrl();
        this.projectUrl = project.getProjectUrl();
        this.configurationIds = nullableStreamOf(project.getBuildConfigurations())
                .map(buildConfiguration -> buildConfiguration.getId())
                .collect(Collectors.toList());
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the issue tracker url.
     *
     * @return the issue tracker url
     */
    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    /**
     * Sets the issue tracker url.
     *
     * @param issueTrackerUrl the new issue tracker url
     */
    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    /**
     * Gets the project url.
     *
     * @return the project url
     */
    public String getProjectUrl() {
        return projectUrl;
    }

    /**
     * Sets the project url.
     *
     * @param projectUrl the new project url
     */
    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    /**
     * Gets the configuration ids.
     *
     * @return the configuration ids
     */
    public List<Integer> getConfigurationIds() {
        return configurationIds;
    }

    /**
     * Sets the configuration ids.
     *
     * @param configurationIds the new configuration ids
     */
    public void setConfigurationIds(List<Integer> configurationIds) {
        this.configurationIds = configurationIds;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public Project.Builder toDBEntityBuilder() {
        Project.Builder builder = Project.Builder.newBuilder()
                .id(id)
                .name(name)
                .description(description)
                .issueTrackerUrl(issueTrackerUrl)
                .projectUrl(projectUrl);

        nullableStreamOf(configurationIds).forEach(
                configurationId -> builder
                        .buildConfiguration(BuildConfiguration.Builder.newBuilder().id(configurationId).build()));

        return builder;
    }

}
