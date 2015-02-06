package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;

/**
 * The Class License maps the different licenses to be linked to the projects, i.e. APACHE 2.0, MIT, GLPL, etc
 */
@Entity
public class License implements Serializable {

    private static final long serialVersionUID = 8893552998204511626L;

    public static final String DEFAULT_SORTING_FIELD = "shortName";

    @Id
    @GeneratedValue
    private Integer id;

    private String fullName;

    @Type(type = "org.hibernate.type.TextType")
    private String fullContent;

    private String refUrl;

    private String shortName;

    // bi-directional many-to-one association to Project
    @OneToMany(mappedBy = "license")
    private List<Project> projects;

    /**
     * Instantiates a new license.
     */
    public License() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return this.id;
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
     * Gets the full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Sets the full name.
     *
     * @param fullName the new full name
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
     * Gets the ref url.
     *
     * @return the ref url
     */
    public String getRefUrl() {
        return this.refUrl;
    }

    /**
     * Sets the ref url.
     *
     * @param refUrl the new ref url
     */
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    /**
     * Gets the short name.
     *
     * @return the short name
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Sets the short name.
     *
     * @param shortName the new short name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Gets the projects.
     *
     * @return the projects
     */
    public List<Project> getProjects() {
        return this.projects;
    }

    /**
     * Sets the projects.
     *
     * @param projects the new projects
     */
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Adds the project.
     *
     * @param project the project
     * @return the project
     */
    public Project addProject(Project project) {
        getProjects().add(project);
        project.setLicense(this);

        return project;
    }

    /**
     * Removes the project.
     *
     * @param project the project
     * @return the project
     */
    public Project removeProject(Project project) {
        getProjects().remove(project);
        project.setLicense(null);

        return project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "License [fullName=" + fullName + "]";
    }

}
