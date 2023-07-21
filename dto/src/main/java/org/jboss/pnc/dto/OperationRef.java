/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.experimental.SuperBuilder;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Artifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.MavenArtifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.NPMArtifact;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.validator.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@SuperBuilder(builderMethodName = "refBuilder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationRef implements DTOEntity {

    /**
     * ID of the build.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * The time when the operation was submited and acreated.
     */
    protected final Instant submitTime;

    /**
     * The time when the operation started.
     */
    protected final Instant startTime;

    /**
     * The time when the build finished.
     */

    protected final Instant endTime;

    /**
     * The progress status of the operation.
     */
    protected final ProgressStatus progressStatus;

    /**
     * The result status of the operation.
     */
    protected final OperationResult result;
}
