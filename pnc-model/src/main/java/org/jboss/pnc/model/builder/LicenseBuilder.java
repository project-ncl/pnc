/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Created by avibelli on Feb 5, 2015
 *
 */

package org.jboss.pnc.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.model.License;
import org.jboss.pnc.model.Project;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
public class LicenseBuilder {

    private Integer id;

    private String fullName;

    private String fullContent;

    private String refUrl;

    private String shortName;

    private List<Project> projects;

    private LicenseBuilder() {
        projects = new ArrayList<>();
    }

    public static LicenseBuilder newBuilder() {
        return new LicenseBuilder();
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

    public LicenseBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public LicenseBuilder fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public LicenseBuilder fullContent(String fullContent) {
        this.fullContent = fullContent;
        return this;
    }

    public LicenseBuilder refUrl(String refUrl) {
        this.refUrl = refUrl;
        return this;
    }

    public LicenseBuilder shortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public LicenseBuilder projects(List<Project> projects) {
        this.projects = projects;
        return this;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the fullContent
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * @return the refUrl
     */
    public String getRefUrl() {
        return refUrl;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @return the projects
     */
    public List<Project> getProjects() {
        return projects;
    }

}
