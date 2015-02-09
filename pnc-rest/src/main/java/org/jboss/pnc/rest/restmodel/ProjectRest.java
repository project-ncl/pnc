package org.jboss.pnc.rest.restmodel;


import org.jboss.pnc.model.Project;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "Project")
public class ProjectRest {

    private Integer id;

    private String name;

    private String description;

    private String issueTrackerUrl;

    private String projectUrl;

    private List<Integer> configurationIds;

    public ProjectRest() {
    }

    public ProjectRest(Project project) {
        this.id = project.getId();
        this.description = project.getDescription();
        this.name = project.getName();
        this.issueTrackerUrl = project.getIssueTrackerUrl();
        this.projectUrl = project.getProjectUrl();
        configurationIds = nullableStreamOf(project.getBuildConfigurations())
                .map(buildConfiguration -> buildConfiguration.getId())
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public List<Integer> getConfigurationIds() {
        return configurationIds;
    }

    public void setConfigurationIds(List<Integer> configurationIds) {
        this.configurationIds = configurationIds;
    }
}
