package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The Class User maps the user that triggered the builds, and are linked to the BuildRecord
 *
 * @author avibelli
 */
@Entity
@Table(name = "Users")
public class User implements Serializable {

    private static final long serialVersionUID = 8437525005838384722L;

    @Id
    @GeneratedValue
    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String username;

    @OneToMany(mappedBy = "user")
    private List<BuildRecord> buildRecords;

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
     * Gets the project build record.
     *
     * @return the project build record
     */
    public List<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    /**
     * Sets the project build record.
     *
     * @param buildRecords the new project build record
     */
    public void setBuildRecords(List<BuildRecord> buildRecords) {
        this.buildRecords = buildRecords;
    }

    /**
     * Adds the project build record.
     *
     * @param buildRecords the project build record
     * @return the project build record
     */
    public BuildRecord addBuildRecord(BuildRecord buildRecord) {

        getBuildRecords().add(buildRecord);
        buildRecord.setUser(this);

        return buildRecord;
    }

    /**
     * Removes the project build record.
     *
     * @param buildRecord the project build record
     * @return the project build record
     */
    public BuildRecord removeBuildRecord(BuildRecord buildRecord) {
        getBuildRecords().remove(buildRecord);
        buildRecord.setUser(null);
        return buildRecord;
    }
}
