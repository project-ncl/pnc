package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.ForeignKey;

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
@Table(name = "project")
public class Project implements Serializable {

    private static final long serialVersionUID = -4644857058640271044L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(name = "issue_tracker_url")
    private String issueTrackerUrl;

    @Column(name = "project_url")
    private String projectUrl;

    @ManyToOne
    @ForeignKey(name = "fk_project_license")
    private License license;

    @Column(name = "product_version_project")
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProductVersionProject> productVersionProject;

    @Column(name = "project_build_configuration")
    @XmlTransient
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectBuildConfiguration> projectBuildConfiguration;

    /**
     * Instantiates a new project.
     */
    public Project() {
        productVersionProject = new HashSet<>();
        projectBuildConfiguration = new HashSet<>();
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
     * @return the productVersionProject
     */
    public Set<ProductVersionProject> getProductVersionProject() {
        return productVersionProject;
    }

    /**
     * @param productVersionProject the productVersionProject to set
     */
    public void setProductVersionProject(Set<ProductVersionProject> productVersionProject) {
        this.productVersionProject = productVersionProject;
    }

    /**
     * @return the projectBuildConfiguration
     */
    public Set<ProjectBuildConfiguration> getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    /**
     * @param projectBuildConfiguration the projectBuildConfiguration to set
     */
    public void setProjectBuildConfiguration(Set<ProjectBuildConfiguration> projectBuildConfiguration) {
        this.projectBuildConfiguration = projectBuildConfiguration;
    }

    /**
     * Add a productVersionProject to the set of productVersionProjects
     *
     * @param productVersionProject
     * @return
     */
    public Set<ProductVersionProject> addProductVersionProject(ProductVersionProject prodVersionProject) {
        productVersionProject.add(prodVersionProject);

        return productVersionProject;
    }

    /**
     * Remove a productVersionProject from the set of productVersionProjects
     *
     * @param productVersionProject
     * @return
     */
    public Set<ProductVersionProject> removeProductVersionProject(ProductVersionProject prodVersionProject) {
        productVersionProject.remove(prodVersionProject);

        return productVersionProject;
    }

    /**
     * Add a projectBuildConfiguration to the set of projectBuildConfigurations
     *
     * @param configuration
     * @return
     */
    public Set<ProjectBuildConfiguration> addProjectBuildConfiguration(ProjectBuildConfiguration configuration) {
        projectBuildConfiguration.add(configuration);

        return projectBuildConfiguration;
    }

    /**
     * Remove a projectBuildConfiguration from the set of projectBuildConfigurations
     *
     * @param configuration
     * @return
     */
    public Set<ProjectBuildConfiguration> removeProjectBuildConfiguration(ProjectBuildConfiguration configuration) {
        projectBuildConfiguration.remove(configuration);

        return projectBuildConfiguration;
    }

    @Override
    public String toString() {
        return "Project [name=" + name + "]";
    }

}
