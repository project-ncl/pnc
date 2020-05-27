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
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(value = "npm")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = NpmBuild.NpmBuildBuilder.class)
public class NpmBuild extends Build {

    @NonNull
    private final String name;

    private final String version;

    @Builder
    public NpmBuild(
            String name,
            String version,
            String buildName,
            String buildVersion,
            String externalBuildSystem,
            Long externalBuildID,
            String externalBuildURL,
            Date startTime,
            Date endTime,
            String scmURL,
            String scmRevision,
            String scmTag,
            BuildRoot buildRoot,
            Set<Logfile> logs,
            Set<Dependency> dependencies,
            Set<BuiltArtifact> builtArtifacts,
            String tagPrefix) {
        super(
                buildName,
                buildVersion,
                externalBuildSystem,
                externalBuildID,
                externalBuildURL,
                startTime,
                endTime,
                scmURL,
                scmRevision,
                buildRoot,
                logs,
                dependencies,
                builtArtifacts,
                tagPrefix);
        this.name = Objects.requireNonNull(name);
        this.version = version;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class NpmBuildBuilder {
    }
}