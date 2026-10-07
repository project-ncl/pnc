/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * The report of the build push.
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED) // TODO: Remove when BuildPushReportCompatibility is removed
public class BuildPushReport implements DTOEntity {

    /**
     * ID of the report (which is in fact the same as the ID of the build push operation by which this report was done).
     */
    private final String id;

    /**
     * The time when the build push was submitted.
     */
    private final Instant submitTime;

    /**
     * The time when the build push was started.
     */
    private final Instant startTime;

    /**
     * The time when the build push finished.
     */
    private final Instant endTime;

    /**
     * The user who started the build push.
     */
    private final User user;

    /**
     * The progress status of the operation.
     */
    private final ProgressStatus progressStatus;

    /**
     * The result status of the operation.
     */
    private final OperationResult result;

    /**
     * The build which was pushed to brew.
     */
    private final BuildRef build;

    /**
     * Tag prefix that was used for the push.
     */
    private final String tagPrefix;

    /**
     * The ID of pushed build in Brew.
     */
    private final Integer brewBuildId;

    /**
     * The URL to the pushed build in Brew.
     */
    private final String brewBuildUrl;
}
