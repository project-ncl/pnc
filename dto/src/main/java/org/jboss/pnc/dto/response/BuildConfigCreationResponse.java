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
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildConfigCreationResponse.Builder.class)
public class BuildConfigCreationResponse {

    private Integer taskId;

    private BuildConfiguration buildConfig;

    public BuildConfigCreationResponse(BuildConfiguration buildConfiguration) {
        this.buildConfig = buildConfiguration;
    }

    public BuildConfigCreationResponse(int taskId) {
        this.taskId = taskId;
    }

    @lombok.Builder(builderClassName = "Builder")
    private BuildConfigCreationResponse(Integer taskId, BuildConfiguration buildConfiguration) {
        this.taskId = taskId;
        this.buildConfig = buildConfiguration;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
