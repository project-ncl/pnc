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
package org.jboss.pnc.model;

import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * The class Project incapsulates the basic properties of a Project, i.e. the name, description, license. It is linked to a list
 * of BuildConfigurations, that contain the build configurations of the Project in its lifetime. The class Project is also
 * linked to a list of buildRecords, that contains the result of the build triggered with a BuildConfiguration
 */
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(name = "uk_project_name", columnNames = {"name"}),
    indexes = {
        @Index(name = "idx_project_license", columnList = "license_id")
    }
)
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
    @Size(max=255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Size(max=255)
    private String issueTrackerUrl;

    @Size(max=255)
    private String projectUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_project_license"))
    private License license;

    @OneToMany(mappedBy = "project", cascade = { CascadeType.REFRESH, CascadeType.REMOVE })
    private Set<BuildConfiguration> buildConfigurations;

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
        this.description = description;
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
        this.issueTrackerUrl = issueTrackerUrl;
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
        this.projectUrl = projectUrl;
    }

    /**
     * @return the license
     */
    public License getLicense() {
        return license;
    }

    /**
     * @param license the license to set
     */
    public void setLicense(License license) {
        this.license = license;
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
     * @return the resulting BuildConfigurations
     */
    public Set<BuildConfiguration> addBuildConfiguration(BuildConfiguration configuration) {
        buildConfigurations.add(configuration);

        return buildConfigurations;
    }

    /**
     * Remove a buildConfiguration from the set of buildConfigurations
     *
     * @param configuration The configuration to remove from this project
     * @return the resulting BuildConfigurations
     */
    public Set<BuildConfiguration> removeBuildConfiguration(BuildConfiguration configuration) {
        buildConfigurations.remove(configuration);

        return buildConfigurations;
    }

    @Override
    public String toString() {
        return "Project [name=" + name + "]";
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String description;

        private String issueTrackerUrl;

        private String projectUrl;

        private License license;

        private Set<BuildConfiguration> buildConfigurations;

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
            project.setLicense(license);

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

        public Builder license(License license) {
            this.license = license;
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

        public Set<BuildConfiguration> getBuildConfigurations() {
            return buildConfigurations;
        }

    }
}
