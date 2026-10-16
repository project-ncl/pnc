/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.api.dto.OperationOutcome;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.SuperBuilder;
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
     * 
     * @deprecated Waiting for UI to use outcome before removal
     */
    @Deprecated()
    protected final OperationResult result;

    /**
     * The result status of the operation including exception resolution if any.
     */
    protected final OperationOutcome outcome;
}
