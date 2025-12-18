/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.jboss.pnc.common.Strings;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 *
 * The class Project incapsulates the basic properties of a Project, i.e. the name, description, license. It is linked
 * to a list of BuildConfigurations, that contain the build configurations of the Project in its lifetime. The class
 * Project is also linked to a list of buildRecords, that contains the result of the build triggered with a
 * BuildConfiguration
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_project_name", columnNames = { "name" }))
public class Project implements GenericEntity<Integer> {

    private static final long serialVersionUID = -4644857058640271044L;

    public static final String DEFAULT_SORTING_FIELD = "name";
    public static final String SEQUENCE_NAME = "project_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Column(unique = true)
    @NotNull
    @Size(max = 255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Size(max = 255)
    private String issueTrackerUrl;

    @Size(max = 255)
    private String projectUrl;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "project", cascade = { CascadeType.REFRESH, CascadeType.REMOVE })
    private Set<BuildConfiguration> buildConfigurations;

    @Size(max = 255)
    private String engineeringTeam;

    @Size(max = 255)
    private String technicalLeader;

    /**
     * Instantiates a new project.
     */
    public Project() {
        buildConfigurations = new HashSet<>();
    }

    /**
     * @return the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = Strings.nullIfBlank(description);
    }

    /**
     * @return the issueTrackerUrl
     */
    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    /**
     * @param issueTrackerUrl the issueTrackerUrl to set
     */
    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = Strings.nullIfBlank(issueTrackerUrl);
    }

    /**
     * @return the projectUrl
     */
    public String getProjectUrl() {
        return projectUrl;
    }

    /**
     * @param projectUrl the projectUrl to set
     */
    public void setProjectUrl(String projectUrl) {
        this.projectUrl = Strings.nullIfBlank(projectUrl);
    }

    /**
     * @return the buildConfigurations
     */
    public Set<BuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

    /**
     * @param buildConfigurations the buildConfigurations to set
     */
    public void setBuildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    /**
     * Add a buildConfiguration to the set of buildConfigurations
     *
     * @param configuration The configuration to add to this project
     */
    public void addBuildConfiguration(BuildConfiguration configuration) {
        buildConfigurations.add(configuration);
        configuration.setProject(this);
    }

    /**
     * Remove a buildConfiguration from the set of buildConfigurations
     *
     * @param configuration The configuration to remove from this project
     */
    public void removeBuildConfiguration(BuildConfiguration configuration) {
        buildConfigurations.remove(configuration);
        configuration.setProject(null);
    }

    /**
     * @return the engineeringTeam in charge of the project
     */
    public String getEngineeringTeam() {
        return engineeringTeam;
    }

    /**
     * @param engineeringTeam the engineeringTeam to set
     */
    public void setEngineeringTeam(String engineeringTeam) {
        this.engineeringTeam = Strings.nullIfBlank(engineeringTeam);
    }

    /**
     * @return the technicalLeader of the project
     */
    public String getTechnicalLeader() {
        return technicalLeader;
    }

    /**
     * @param technicalLeader the technicalLeader to set
     */
    public void setTechnicalLeader(String technicalLeader) {
        this.technicalLeader = Strings.nullIfBlank(technicalLeader);
    }

    @Override
    public String toString() {
        return "Project [name=" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Project))
            return false;
        return id != null && id.equals(((Project) o).getId());
    }

    @Override
    public int hashCode() {
        // Because the id is generated when the entity is stored to DB, we need to have constant hash code to achieve
        // equals+hashCode consistency across all JPA object states
        return 31;
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String issueTrackerUrl;

        private String projectUrl;

        private Set<BuildConfiguration> buildConfigurations;

        private String engineeringTeam;

        private String technicalLeader;

        private Builder() {
            buildConfigurations = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Project build() {

            Project project = new Project();
            project.setId(id);
            project.setName(name);
            project.setDescription(description);
            project.setIssueTrackerUrl(issueTrackerUrl);
            project.setProjectUrl(projectUrl);
            project.setEngineeringTeam(engineeringTeam);
            project.setTechnicalLeader(technicalLeader);

            // Set the bi-directional mapping
            for (BuildConfiguration buildConfiguration : buildConfigurations) {
                buildConfiguration.setProject(project);
            }
            project.setBuildConfigurations(buildConfigurations);

            return project;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder issueTrackerUrl(String issueTrackerUrl) {
            this.issueTrackerUrl = issueTrackerUrl;
            return this;
        }

        public Builder projectUrl(String projectUrl) {
            this.projectUrl = projectUrl;
            return this;
        }

        public Builder buildConfiguration(BuildConfiguration buildConfiguration) {
            this.buildConfigurations.add(buildConfiguration);
            return this;
        }

        public Builder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
            this.buildConfigurations = buildConfigurations;
            return this;
        }

        public Builder engineeringTeam(String engineeringTeam) {
            this.engineeringTeam = engineeringTeam;
            return this;
        }

        public Builder technicalLeader(String technicalLeader) {
            this.technicalLeader = technicalLeader;
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

        public Set<BuildConfiguration> getBuildConfigurations() {
            return buildConfigurations;
        }

        public String getEngineeringTeam() {
            return engineeringTeam;
        }

        public String getTechnicalLeader() {
            return technicalLeader;
        }

    }
}
