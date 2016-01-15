/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class License maps the different licenses to be linked to the projects, i.e. APACHE 2.0, MIT, GLPL, etc
 */
@Entity
public class License implements GenericEntity<Integer> {

    private static final long serialVersionUID = 8893552998204511626L;

    public static final String DEFAULT_SORTING_FIELD = "shortName";
    public static final String SEQUENCE_NAME = "license_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @NotNull
    private String fullName;

    @NotNull
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
    @Override
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

    public static class Builder {

        private Integer id;

        private String fullName;

        private String fullContent;

        private String refUrl;

        private String shortName;

        private List<Project> projects;

        private Builder() {
            projects = new ArrayList<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public License build() {

            License license = new License();
            license.setId(id);
            license.setFullName(fullName);
            license.setFullContent(fullContent);
            license.setRefUrl(refUrl);
            license.setShortName(shortName);

            // Set the bi-directional mapping
            for (Project project : projects) {
                project.setLicense(license);
            }
            license.setProjects(projects);

            return license;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder fullContent(String fullContent) {
            this.fullContent = fullContent;
            return this;
        }

        public Builder refUrl(String refUrl) {
            this.refUrl = refUrl;
            return this;
        }

        public Builder shortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder projects(List<Project> projects) {
            this.projects = projects;
            return this;
        }

    }
}
