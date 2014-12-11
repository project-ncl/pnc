package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The Class License maps the different licenses to be linked to the projects, i.e. APACHE 2.0, MIT, GLPL, etc
 */
@Entity
@Table(name = "license")
public class License implements Serializable {

    private static final long serialVersionUID = 8893552998204511626L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "full_name")
    private String fullName;

    @Lob
    @Column(nullable = false, length = 4096, name = "full_content")
    private String fullContent;

    @Column(name = "ref_url")
    private String refUrl;

    @Column(name = "short_name")
    private String shortName;

    @OneToMany(mappedBy = "license")
    private List<Project> project;

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
     * @return the project
     */
    public List<Project> getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(List<Project> project) {
        this.project = project;
    }

    /**
     * Adds the project.
     *
     * @param project the project
     * @return the project
     */
    public Project addProject(Project project) {
        getProject().add(project);
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
        getProject().remove(project);
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
