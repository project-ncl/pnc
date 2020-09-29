/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * The Class User maps the user that triggered the builds, and are linked to the BuildRecord
 *
 * @author avibelli
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        name = "UserTable",
        uniqueConstraints = { @UniqueConstraint(name = "uk_user_email", columnNames = { "email" }),
                @UniqueConstraint(name = "uk_user_username", columnNames = { "username" }) })
public class User implements GenericEntity<Integer> {

    private static final long serialVersionUID = 8437525005838384722L;

    public static final String DEFAULT_SORTING_FIELD = "username";
    public static final String SEQUENCE_NAME = "user_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Column(unique = true)
    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    /**
     * OAUTH token, used to pass around. Property is set once user is authenticated, note that having a token doesn't
     * necessary mean user is logged-in a token needs to be validated.
     */
    @Transient
    private String loginToken;

    @Column(unique = true)
    @NotNull
    @Size(max = 50)
    private String username;

    @OneToMany(mappedBy = "user")
    private Set<BuildRecord> buildRecords;

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
    @Override
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    @Override
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
    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    /**
     * Sets the project build record.
     *
     * @param buildRecords the new project build record
     */
    public void setBuildRecords(Set<BuildRecord> buildRecords) {
        this.buildRecords = buildRecords;
    }

    /**
     * Adds the project build record.
     *
     * @param buildRecord the project build record
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
            if (other.getUsername() != null) {
                return false;
            }
        } else if (!username.equals(other.getUsername())) {
            return false;
        }
        return true;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", email='" + email + '\'' + ", firstName='" + firstName + '\'' + ", lastName='"
                + lastName + '\'' + ", loginToken='" + loginToken + '\'' + ", username='" + username + '\'' + '}';
    }

    public static class Builder {

        private Integer id;

        private String email;

        private String firstName;

        private String lastName;

        private String username;

        private Set<BuildRecord> buildRecords;

        private Builder() {
            buildRecords = new HashSet<>();
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

        public Builder buildRecords(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

    }

}
