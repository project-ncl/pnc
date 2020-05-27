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
package org.jboss.pnc.causewayclient.remotespi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@buildType")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSubTypes({ @JsonSubTypes.Type(MavenBuild.class), @JsonSubTypes.Type(NpmBuild.class) })
public abstract class Build {

    @NonNull
    private final String buildName;

    private final String buildVersion;
    @NonNull
    private final String externalBuildSystem;
    private final Long externalBuildID;
    @NonNull
    private final String externalBuildURL;
    @NonNull
    private final Date startTime;
    @NonNull
    private final Date endTime;
    @NonNull
    private final String scmURL;
    @NonNull
    private final String scmRevision;
    @NonNull
    private final BuildRoot buildRoot;
    @NonNull
    private final Set<Logfile> logs;
    @NonNull
    private final Set<Dependency> dependencies;
    @NonNull
    private final Set<BuiltArtifact> builtArtifacts;
    @NonNull
    private final String tagPrefix;

    // We use IDE generated constructor instead of lombok.AllArgsConstructor because of nicer
    // parametr names.
    protected Build(
            String buildName,
            String buildVersion,
            String externalBuildSystem,
            Long externalBuildID,
            String externalBuildURL,
            Date startTime,
            Date endTime,
            String scmURL,
            String scmRevision,
            BuildRoot buildRoot,
            Set<Logfile> logs,
            Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts,
            String tagPrefix) {
        this.buildName = buildName;
        this.buildVersion = buildVersion;
        this.externalBuildSystem = externalBuildSystem;
        this.externalBuildID = externalBuildID;
        this.externalBuildURL = externalBuildURL;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scmURL = scmURL;
        this.scmRevision = scmRevision;
        this.buildRoot = buildRoot;
        this.logs = logs;
        this.dependencies = dependencies;
        this.builtArtifacts = builtArtifacts;
        this.tagPrefix = tagPrefix;
    }

}
