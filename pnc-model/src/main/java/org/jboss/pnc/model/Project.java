package org.jboss.pnc.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
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
public class Project implements Serializable {

    private static final long serialVersionUID = -4644857058640271044L;

    public static final String DEFAULT_SORTING_FIELD = "name";

    @Id
    @SequenceGenerator(name="project_id_seq", sequenceName="project_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="project_id_seq")
    private Integer id;

    @NotNull
    private String name;

    private String description;

    private String issueTrackerUrl;

    private String projectUrl;

    @ManyToOne
    private License license;

    @OneToMany(mappedBy = "project", cascade={CascadeType.REFRESH, CascadeType.REMOVE})
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
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
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
     * @param configuration
     * @return
     */
    public Set<BuildConfiguration> addBuildConfiguration(BuildConfiguration configuration) {
        buildConfigurations.add(configuration);

        return buildConfigurations;
    }

    /**
     * Remove a buildConfiguration from the set of buildConfigurations
     *
     * @param configuration
     * @return
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
