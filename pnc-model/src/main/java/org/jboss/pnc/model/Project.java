package org.jboss.pnc.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * The class Project incapsulates the basic properties of a Project, i.e. the name, description, license. It is linked to a list
 * of ProjectBuildConfigurations, that contain the build configurations of the Project in its lifetime. The class Project is
 * also linked to a list of ProjectBuildResults, that contains the result of the build triggered with a
 * ProjectBuildConfiguration
 */
@XmlRootElement
@Entity
public class Project implements Serializable {

    private static final long serialVersionUID = -4644857058640271044L;

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String description;

    private String issueTrackerUrl;

    private String projectUrl;

    @ManyToOne
    private License license;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProductVersionProject> productVersionProjects;

    @XmlTransient
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectBuildConfiguration> projectBuildConfigurations;

    /**
     * Instantiates a new project.
     */
    public Project() {
        productVersionProjects = new HashSet<>();
        projectBuildConfigurations = new HashSet<>();
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
     * @return the productVersionProjects
     */
    public Set<ProductVersionProject> getProductVersionProjects() {
        return productVersionProjects;
    }

    /**
     * @param productVersionProjects the productVersionProjects to set
     */
    public void setProductVersionProjects(Set<ProductVersionProject> productVersionProjects) {
        this.productVersionProjects = productVersionProjects;
    }

    /**
     * Add a projectBuildConfiguration to the set of projectBuildConfigurations
     *
     * @param configuration
     * @return
     */
    public Set<ProjectBuildConfiguration> addProjectBuildConfiguration(ProjectBuildConfiguration configuration) {
        projectBuildConfigurations.add(configuration);

        return projectBuildConfigurations;
    }

    /**
     * Remove a projectBuildConfiguration from the set of projectBuildConfigurations
     *
     * @param configuration
     * @return
     */
    public Set<ProjectBuildConfiguration> removeProjectBuildConfiguration(ProjectBuildConfiguration configuration) {
        projectBuildConfigurations.remove(configuration);

        return projectBuildConfigurations;
    }

    @Override
    public String toString() {
        return "Project [name=" + name + "]";
    }

}
