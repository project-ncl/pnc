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
package org.jboss.pnc.dto.response;

import org.jboss.pnc.dto.BuildConfiguration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 * The result of Create&Sync call for creating build config with scm url. If the SCM repository config can be created
 * immediately, new build config will be also created immediately and returned by {@link #getBuildConfig()} property. If
 * the repository needs to be synchronized first, {@link #getTaskId()} property provides id of the synchronization task.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildConfigCreationResponse.Builder.class)
public class BuildConfigCreationResponse {

    /**
     * Id of the task that will create and sync the repository and create build config. When the repository doesn't
     * require sync, this is null and {@link #getBuildConfig()} is returned instead.
     */
    private Integer taskId;

    /**
     * The created build config. When the repository require sync, this is null and {@link #getTaskId()} is returned
     * instead.
     */
    private BuildConfiguration buildConfig;

    public BuildConfigCreationResponse(BuildConfiguration buildConfiguration) {
        this.buildConfig = buildConfiguration;
    }

    public BuildConfigCreationResponse(int taskId) {
        this.taskId = taskId;
    }

    @lombok.Builder(builderClassName = "Builder")
    private BuildConfigCreationResponse(Integer taskId, BuildConfiguration buildConfig) {
        this.taskId = taskId;
        this.buildConfig = buildConfig;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
