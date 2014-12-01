package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * The class Project incapsulates the basic properties of a Project, i.e. the name, description, license. It is linked to a list
 * of ProjectBuildConfigurations, that contain the build configurations of the Project in its lifetime. The class Project is
 * also linked to a list of ProjectBuildResults, that contains the result of the build triggered with a
 * ProjectBuildConfiguration
 */
@Entity
@NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p")
public class Project implements Serializable {

    private static final long serialVersionUID = -4644857058640271044L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String description;

    @Column(name = "issue_tracker_url")
    private String issueTrackerUrl;

    @Column(name = "project_url")
    private String projectUrl;

    @ManyToOne
    @JoinColumn(name = "current_license_id")
    private License license;

    @OneToMany(mappedBy = "project")
    private Set<ProjectBuildConfiguration> projectBuildConfigurations;

    @OneToMany(mappedBy = "project")
    private List<ProjectBuildResult> projectBuildResults;

    /**
     * Instantiates a new project.
     */
    public Project() {
        this.projectBuildConfigurations = new HashSet<ProjectBuildConfiguration>();
    }

    /**
     * Instantiates a new project.
     *
     * @param name the name
     * @param description the description
     * @param issueTrackerUrl the issue tracker url
     * @param projectUrl the project url
     * @param license the license
     * @param projectBuildConfigurations the project build configurations
     */
    public Project(String name, String description, String issueTrackerUrl, String projectUrl, License license,
            ProjectBuildConfiguration... projectBuildConfigurations) {

        this.name = name;
        this.description = description;
        this.issueTrackerUrl = issueTrackerUrl;
        this.projectUrl = projectUrl;
        this.license = license;
        this.projectBuildConfigurations = new HashSet<ProjectBuildConfiguration>(Arrays.asList(projectBuildConfigurations));
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
     * @return the projectBuildConfigurations
     */
    public Set<ProjectBuildConfiguration> getProjectBuildConfigurations() {
        return projectBuildConfigurations;
    }

    /**
     * @param projectBuildConfigurations the projectBuildConfigurations to set
     */
    public void setProjectBuildConfigurations(Set<ProjectBuildConfiguration> projectBuildConfigurations) {
        this.projectBuildConfigurations = projectBuildConfigurations;
    }

    /**
     * @return the projectBuildResults
     */
    public List<ProjectBuildResult> getProjectBuildResults() {
        return projectBuildResults;
    }

    /**
     * @param projectBuildResults the projectBuildResults to set
     */
    public void setProjectBuildResults(List<ProjectBuildResult> projectBuildResults) {
        this.projectBuildResults = projectBuildResults;
    }

    public Set<ProjectBuildConfiguration> addProjectBuildConfiguration(ProjectBuildConfiguration configuration) {
        projectBuildConfigurations.add(configuration);

        return projectBuildConfigurations;
    }

    public Set<ProjectBuildConfiguration> removeProjectBuildConfiguration(ProjectBuildConfiguration configuration) {

        projectBuildConfigurations.remove(configuration);

        return projectBuildConfigurations;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Project)) {
            return false;
        }

        Project p = (Project) o;
        return this.id.equals(p.getId());
    }

    @Override
    public String toString() {
        return "Project [name=" + name + "]";
    }

}
