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
package org.jboss.pnc.rest.restmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;
import java.util.Map;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildConfigurationAudited")
@ToString
public class BuildConfigurationAuditedRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private Integer rev;

    // Required for rsql queries
    private IdRev idRev;

    private String name;

    private String buildScript;

    @Setter
    @Getter
    @NotNull
    private RepositoryConfigurationRest repositoryConfiguration;

    private String scmRevision;

    private Date creationTime;

    private Date lastModificationTime;

    private Integer projectId;

    private Integer environmentId;

    private ProjectRest project;

    private BuildEnvironmentRest environment;

    @Getter
    @Setter
    private Map<String, String> genericParameters;

    private UserRest creationUser;

    private UserRest lastModificationUser;

    public BuildConfigurationAuditedRest() {
    }

    public BuildConfigurationAuditedRest(BuildConfigurationAudited buildConfigurationAudited) {
        this.idRev = buildConfigurationAudited.getIdRev();
        this.id = buildConfigurationAudited.getIdRev().getId();
        this.rev = buildConfigurationAudited.getRev();
        this.name = buildConfigurationAudited.getName();
        this.buildScript = buildConfigurationAudited.getBuildScript();
        this.scmRevision = buildConfigurationAudited.getScmRevision();
        this.creationTime = buildConfigurationAudited.getCreationTime();
        this.lastModificationTime = buildConfigurationAudited.getLastModificationTime();
        genericParameters = buildConfigurationAudited.getGenericParameters();

        performIfNotNull(
                buildConfigurationAudited.getRepositoryConfiguration(),
                () -> this.repositoryConfiguration = new RepositoryConfigurationRest(
                        buildConfigurationAudited.getRepositoryConfiguration()));
        performIfNotNull(
                buildConfigurationAudited.getProject(),
                () -> this.project = new ProjectRest(buildConfigurationAudited.getProject()));
        performIfNotNull(
                buildConfigurationAudited.getBuildEnvironment(),
                () -> this.environment = new BuildEnvironmentRest(buildConfigurationAudited.getBuildEnvironment()));

        performIfNotNull(this.project, () -> this.projectId = this.project.getId());
        performIfNotNull(this.environment, () -> this.environmentId = this.environment.getId());
        performIfNotNull(
                buildConfigurationAudited.getCreationUser(),
                () -> this.creationUser = new UserRest(buildConfigurationAudited.getCreationUser()));
        performIfNotNull(
                buildConfigurationAudited.getLastModificationUser(),
                () -> this.lastModificationUser = new UserRest(buildConfigurationAudited.getLastModificationUser()));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public IdRev getIdRev() {
        return idRev;
    }

    public void setIdRev(IdRev idRev) {
        this.idRev = idRev;
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

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
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

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public ProjectRest getProject() {
        return project;
    }

    public void setProject(ProjectRest project) {
        this.project = project;
    }

    public BuildEnvironmentRest getEnvironment() {
        return environment;
    }

    public void setEnvironment(BuildEnvironmentRest environment) {
        this.environment = environment;
    }

    public UserRest getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(UserRest creationUser) {
        this.creationUser = creationUser;
    }

    public UserRest getLastModificationUser() {
        return lastModificationUser;
    }

    public void setLastModificationUser(UserRest lastModificationUser) {
        this.lastModificationUser = lastModificationUser;
    }

    @XmlTransient
    public BuildConfigurationAudited.Builder toDBEntityBuilder() {

        BuildConfiguration.Builder buildConfigBuilder = BuildConfiguration.Builder.newBuilder()
                .id(id)
                .name(name)
                .buildScript(buildScript)
                .scmRevision(scmRevision)
                .creationTime(creationTime)
                .lastModificationTime(lastModificationTime)
                .genericParameters(genericParameters);

        performIfNotNull(
                this.getRepositoryConfiguration(),
                () -> this.getRepositoryConfiguration().toDBEntityBuilder().build());
        performIfNotNull(this.project, () -> buildConfigBuilder.project(this.project.toDBEntityBuilder().build()));
        performIfNotNull(
                this.environment,
                () -> buildConfigBuilder.buildEnvironment(this.environment.toDBEntityBuilder().build()));
        performIfNotNull(
                this.creationUser,
                () -> buildConfigBuilder.creationUser(this.creationUser.toDBEntityBuilder().build()));
        performIfNotNull(
                this.lastModificationUser,
                () -> buildConfigBuilder.lastModificationUser(this.lastModificationUser.toDBEntityBuilder().build()));

        BuildConfigurationAudited.Builder builder = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfigBuilder.build())
                .rev(rev);
        return builder;
    }

}
