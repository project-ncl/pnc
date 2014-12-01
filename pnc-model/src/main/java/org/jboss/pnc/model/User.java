package org.jboss.pnc.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * The Class User maps the user that triggered the builds, and are linked to the ProjectBuildResult
 *
 * @author avibelli
 */
@Entity
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class User implements Serializable {

    private static final long serialVersionUID = 8437525005838384722L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String username;

    @OneToMany
    private List<ProjectBuildResult> projectBuildResults;

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
     * Gets the project build results.
     *
     * @return the project build results
     */
    public List<ProjectBuildResult> getProjectBuildResults() {
        return projectBuildResults;
    }

    /**
     * Sets the project build results.
     *
     * @param projectBuildResults the new project build results
     */
    public void setProjectBuildResults(List<ProjectBuildResult> projectBuildResults) {
        this.projectBuildResults = projectBuildResults;
    }

    /**
     * Adds the project build result.
     *
     * @param projectBuildResult the project build result
     * @return the project build result
     */
    public ProjectBuildResult addProjectBuildResult(ProjectBuildResult projectBuildResult) {

        getProjectBuildResults().add(projectBuildResult);
        projectBuildResult.setUser(this);

        return projectBuildResult;
    }

    /**
     * Removes the project build result.
     *
     * @param projectBuildResult the project build result
     * @return the project build result
     */
    public ProjectBuildResult removeProjectBuildResult(ProjectBuildResult projectBuildResult) {
        getProjectBuildResults().remove(projectBuildResult);
        projectBuildResult.setUser(null);
        return projectBuildResult;
    }
}
