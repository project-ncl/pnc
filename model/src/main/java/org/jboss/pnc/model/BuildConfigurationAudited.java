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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The audited record of a build configuration. Each change to the build configuration table is recorded in the audit table.
 * This class serves to access the data of a specific version of a build configuration.
 * Keep in mind that it is not managed by JPA and needs to be filled manually.
 *
 */
public class BuildConfigurationAudited {

    /**
     * The id of the build configuration this record is associated with
     */
    private Integer id;

    /**
     * The table revision which identifies version of the build config
     */
    private Integer rev;

    private IdRev idRev;

    private String name;

    private String buildScript;

    private RepositoryConfiguration repositoryConfiguration;

    private String scmRevision;

    private Project project;

    private BuildType buildType;

    private BuildEnvironment buildEnvironment;

    private Date creationTime;

    private Date lastModificationTime;
    private BuildConfiguration buildConfiguration;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfigurationAudited() {
    }

    private Map<String, String> genericParameters = new HashMap<>();

    public IdRev getIdRev() {
        return idRev;
    }

    public void setIdRev(IdRev idRev) {
        this.idRev = idRev;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public BuildEnvironment getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(BuildEnvironment buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    public void setBuildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
    }

    public Map<String, String> getGenericParameters() {
        return genericParameters;
    }

    public void setGenericParameters(Map<String, String> genericParameters) {
        this.genericParameters = genericParameters;
    }

    @Override
    public String toString() {
        return "BuildConfigurationAudit [project=" + project + ", name=" + name + ", id=" + id + ", rev=" + rev + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BuildConfigurationAudited that = (BuildConfigurationAudited) o;

        return (idRev != null ? idRev.equals(that.idRev) : false);
    }

    @Override
    public int hashCode() {
        return idRev != null ? idRev.hashCode() : 0;
    }

    public static class Builder {
        private BuildConfiguration buildConfiguration;
        private Integer rev;

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildConfigurationAudited build() {
            BuildConfigurationAudited configurationAudited = new BuildConfigurationAudited();
            configurationAudited.setId(buildConfiguration.getId());
            configurationAudited.setRev(rev);
            configurationAudited.setIdRev(new IdRev(buildConfiguration.getId(), rev));
            configurationAudited.setBuildScript(buildConfiguration.getBuildScript());
            configurationAudited.setBuildEnvironment(buildConfiguration.getBuildEnvironment());
            configurationAudited.setName(buildConfiguration.getName());
            configurationAudited.setScmRevision(buildConfiguration.getScmRevision());
            configurationAudited.setGenericParameters(buildConfiguration.getGenericParameters());
            configurationAudited.setProject(buildConfiguration.getProject());
            configurationAudited.setBuildType(buildConfiguration.getBuildType());
            configurationAudited.setRepositoryConfiguration(buildConfiguration.getRepositoryConfiguration());
            configurationAudited.setCreationTime(buildConfiguration.getCreationTime());
            configurationAudited.setLastModificationTime(buildConfiguration.getLastModificationTime());
            configurationAudited.buildConfiguration = buildConfiguration;
            return configurationAudited;
        }

        public Builder buildConfiguration(BuildConfiguration buildConfiguration) {
            this.buildConfiguration = buildConfiguration;
            return this;
        }

        public Builder rev(Integer rev) {
            this.rev = rev;
            return this;
        }

    }

    public static BuildConfigurationAudited fromBuildConfiguration(BuildConfiguration buildConfiguration, Integer revision) {

        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .rev(revision)
                .build();

        return buildConfigurationAudited;
    }



}
