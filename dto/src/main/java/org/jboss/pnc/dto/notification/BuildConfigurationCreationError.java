/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.Getter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonTypeName(value = BuildConfigurationCreationError.BC_CREATION_ERROR)
public class BuildConfigurationCreationError extends Notification {
    static final String BC_CREATION_ERROR = "BC_CREATION_ERROR";

    public BuildConfigurationCreationError(Integer repositoryId, Integer buildConfigurationId, String errorMessage) {
        super(BC_CREATION_ERROR);
        this.repositoryId = repositoryId;
        this.buildConfigurationId = buildConfigurationId;
        this.errorMessage = errorMessage;
    }

    @Getter
    private final Integer repositoryId;

    @Getter
    private final Integer buildConfigurationId;

    @Getter
    private final String errorMessage;
}
