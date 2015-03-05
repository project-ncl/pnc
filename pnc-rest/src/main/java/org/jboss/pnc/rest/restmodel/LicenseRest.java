package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.License;
import org.jboss.pnc.model.Project;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@XmlRootElement(name = "License")
public class LicenseRest {

    private Integer id;

    private String fullName;

    private String fullContent;

    private String refUrl;

    private String shortName;

    private List<Integer> projectsIds;

    public LicenseRest() {

    }

    public LicenseRest(License license) {
        this.id = license.getId();
        this.fullName = license.getFullName();
        this.fullContent = license.getFullContent();
        this.refUrl = license.getRefUrl();
        this.shortName = license.getShortName();
        this.projectsIds = nullableStreamOf(license.getProjects()).map(project -> project.getId()).collect(Collectors.toList());
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
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the fullContent
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * @param fullContent the fullContent to set
     */
    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }

    /**
     * @return the refUrl
     */
    public String getRefUrl() {
        return refUrl;
    }

    /**
     * @param refUrl the refUrl to set
     */
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the projects
     */
    public List<Integer> getProjectsIds() {
        return projectsIds;
    }

    /**
     * @param projectsIds the projects to set
     */
    public void setProjectsIds(List<Integer> projectsIds) {
        this.projectsIds = projectsIds;
    }

    public License toLicense() {
        List<Project> projects = nullableStreamOf(projectsIds)
                .map(projectId -> Project.Builder.newBuilder().id(projectId).build())
                .collect(Collectors.toList());
        License license = License.Builder.newBuilder()
            .id(id).fullName(fullName)
            .fullContent(fullContent).refUrl(refUrl)
            .shortName(shortName).projects(projects).build();

        return license;
    }

}
