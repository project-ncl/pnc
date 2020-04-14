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

import org.jboss.pnc.dto.SCMRepository;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = RepositoryCreationResponse.Builder.class)
public class RepositoryCreationResponse {

    private Integer taskId;

    private SCMRepository repository;

    public RepositoryCreationResponse(int taskId) {
        this.taskId = taskId;
    }

    public RepositoryCreationResponse(SCMRepository repository) {
        this.repository = repository;
    }

    @lombok.Builder(builderClassName = "Builder")
    private RepositoryCreationResponse(Integer taskId, SCMRepository repository) {
        this.taskId = taskId;
        this.repository = repository;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
