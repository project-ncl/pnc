package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * The Class License maps the different licenses to be linked to the projects, i.e. APACHE 2.0, MIT, GLPL, etc
 */
@Entity
@NamedQuery(name = "License.findAll", query = "SELECT l FROM License l")
public class License implements Serializable {

    private static final long serialVersionUID = 8893552998204511626L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "full_text")
    private String fullText;

    @Column(name = "ref_url")
    private String refUrl;

    @Column(name = "short_name")
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
     * Gets the full text.
     *
     * @return the full text
     */
    public String getFullText() {
        return this.fullText;
    }

    /**
     * Sets the full text.
     *
     * @param fullText the new full text
     */
    public void setFullText(String fullText) {
        this.fullText = fullText;
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
