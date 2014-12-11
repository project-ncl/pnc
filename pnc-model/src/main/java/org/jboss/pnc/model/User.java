package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The Class User maps the user that triggered the builds, and are linked to the ProjectBuildResult
 *
 * @author avibelli
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 8437525005838384722L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false, length = 100)
    private String username;

    @OneToMany(mappedBy = "user")
    private List<ProjectBuildResult> projectBuildResult;

    /**
     * Instantiates a new user.
     */
    public User() {
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
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the email.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the projectBuildResult
     */
    public List<ProjectBuildResult> getProjectBuildResult() {
        return projectBuildResult;
    }

    /**
     * @param projectBuildResult the projectBuildResult to set
     */
    public void setProjectBuildResult(List<ProjectBuildResult> projectBuildResult) {
        this.projectBuildResult = projectBuildResult;
    }

    /**
     * Adds the project build result.
     *
     * @param projectBuildResult the project build result
     * @return the project build result
     */
    public void addProjectBuildResult(ProjectBuildResult projBuildResult) {

        getProjectBuildResult().add(projBuildResult);
        projBuildResult.setUser(this);
    }

    /**
     * Removes the project build result.
     *
     * @param projectBuildResult the project build result
     * @return the project build result
     */
    public void removeProjectBuildResult(ProjectBuildResult projectBuildResult) {
        getProjectBuildResult().remove(projectBuildResult);
        projectBuildResult.setUser(null);
    }
}
