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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The audited record of a build configuration. Each change to the build configuration table is recorded in the audit table.
 * This class serves to access the data of a specific version of a build configuration.
 * Keep in mind that it is not managed by JPA and needs to be filled manually.
 *
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BuildConfigurationAudited {

    private static final long serialVersionUID = 0L;

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

    private String description;

    private Project project;

    private BuildEnvironment buildEnvironment;

    private Set<BuildRecord> buildRecords;

    @Getter
    private BuildConfiguration buildConfiguration;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfigurationAudited() {
    }

    public IdRev getIdRev() {
        return idRev;
    }

    public void setIdRev(IdRev idRev) {
        this.idRev = idRev;
    }

    private Map<String, String> genericParameters = new HashMap<>();

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

        private Set<BuildRecord> buildRecords;

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildConfigurationAudited build() {
            BuildConfigurationAudited configurationAudited = new BuildConfigurationAudited();
            configurationAudited.setId(buildConfiguration.getId());
            configurationAudited.setRev(rev);
            configurationAudited.setIdRev(new IdRev(buildConfiguration.getId(), rev));
            configurationAudited.setBuildScript(buildConfiguration.getBuildScript());
            configurationAudited.setDescription(buildConfiguration.getDescription());
            configurationAudited.setBuildEnvironment(buildConfiguration.getBuildEnvironment());
            configurationAudited.setName(buildConfiguration.getName());
            configurationAudited.setDescription(buildConfiguration.getDescription());
            configurationAudited.setScmRevision(buildConfiguration.getScmRevision());
            configurationAudited.setGenericParameters(buildConfiguration.getGenericParameters());
            configurationAudited.setProject(buildConfiguration.getProject());
            configurationAudited.setRepositoryConfiguration(buildConfiguration.getRepositoryConfiguration());
            configurationAudited.setBuildRecords(buildRecords);
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

        public Builder buildRecords(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }
    }

    public static BuildConfigurationAudited fromBuildConfiguration(BuildConfiguration buildConfiguration, Integer revision) {
        return fromBuildConfiguration(buildConfiguration, revision, Collections.EMPTY_LIST);
    }

    public static BuildConfigurationAudited fromBuildConfiguration(BuildConfiguration buildConfiguration, Integer revision, List<BuildRecord> buildRecords) {
        Map<IdRev, Set<BuildRecord>> buildRecordsByIdRev = buildRecords.stream().collect(Collectors.groupingBy(BuildRecord::getBuildConfigurationAuditedIdRev, Collectors.toSet()));

        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfiguration)
                .rev(revision)
                .buildRecords(buildRecordsByIdRev.get(new IdRev(buildConfiguration.getId(), revision)))
                .build();

        return buildConfigurationAudited;
    }



}
