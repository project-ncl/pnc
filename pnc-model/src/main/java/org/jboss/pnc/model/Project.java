package org.jboss.pnc.model;

import javax.persistence.*;
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

    @OneToMany(mappedBy = "project")
    private Set<BuildConfiguration> buildConfigurations;

    /**
     * Instantiates a new project.
     */
    public Project() {
        productVersionProjects = new HashSet<>();
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
     * Add a productVersionProject to the set of productVersionProjects
     *
     * @param productVersionProject
     * @return
     */
    public Set<ProductVersionProject> addProductVersionProject(ProductVersionProject productVersionProject) {
        productVersionProjects.add(productVersionProject);

        return productVersionProjects;
    }

    /**
     * Remove a productVersionProject from the set of productVersionProjects
     *
     * @param productVersionProject
     * @return
     */
    public Set<ProductVersionProject> removeProductVersionProject(ProductVersionProject productVersionProject) {
        productVersionProjects.remove(productVersionProject);

        return productVersionProjects;
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

}
