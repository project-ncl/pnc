package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.model.builder.LicenseBuilder;
import org.jboss.pnc.model.builder.ProjectBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "Project")
public class ProjectRest {

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
        this.configurationIds = nullableStreamOf(project.getBuildConfigurations()).map(
                buildConfiguration -> buildConfiguration.getId()).collect(Collectors.toList());
        performIfNotNull(project.getLicense() != null, () -> this.licenseId = project.getLicense().getId());
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
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

    /**
     * Gets the project.
     *
     * @return the project
     */
    public Project toProject() {
        ProjectBuilder builder = ProjectBuilder.newBuilder();
        builder.id(id);
        builder.name(name);
        builder.description(description);
        builder.issueTrackerUrl(issueTrackerUrl);
        builder.projectUrl(projectUrl);

        performIfNotNull(this.licenseId != null, () -> builder.license(LicenseBuilder.newBuilder().id(licenseId).build()));

        nullableStreamOf(configurationIds).forEach(configurationId ->
                builder.buildConfiguration(BuildConfigurationBuilder.newBuilder().id(configurationId).build()));

        return builder.build();
    }

}
