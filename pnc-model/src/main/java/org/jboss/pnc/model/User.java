package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * The Class User maps the user that triggered the builds, and are linked to the BuildRecord
 *
 * @author avibelli
 */
@Entity
@Table(name = "Users")
public class User implements Serializable {

    private static final long serialVersionUID = 8437525005838384722L;

    public static final String DEFAULT_SORTING_FIELD = "username";

    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    private String email;

    private String firstName;

    private String lastName;

    @NotNull
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    public static class Builder {

        private Integer id;

        private String email;

        private String firstName;

        private String lastName;

        private String username;

        private List<BuildRecord> buildRecords;

        private Builder() {
            buildRecords = new ArrayList<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public User build() {

            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);

            // Set the bi-directional mapping
            for (BuildRecord buildRecord : buildRecords) {
                buildRecord.setUser(user);
            }
            user.setBuildRecords(buildRecords);

            return user;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecords.add(buildRecord);
            return this;
        }

        public Builder buildRecords(List<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

    }

}
