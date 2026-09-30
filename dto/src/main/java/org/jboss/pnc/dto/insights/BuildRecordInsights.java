/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.insights;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * The build record insights to be used for custom statistics dashboards.
 *
 * @author Andrea Vibelli &lt;avibelli@redhat.com&gt;
 */
@Data
@Jacksonized
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildRecordInsights {

    /**
     * ID of the build.
     */
    private final Long buildId;

    /**
     * Build content ID
     */
    private final String buildContentId;

    /**
     * The time when the build was submitted for building.
     */
    private final Instant submitTime;

    /**
     * The time when the build started building.
     */
    private final Instant startTime;

    /**
     * The time when the build finished building.
     */
    private final Instant endTime;

    /**
     * The time when the build was inserted or last updated
     */
    private final Instant lastUpdateTime;

    /**
     * The year when the build was submited for building.
     */
    private final Integer submitYear;

    /**
     * The month when the build was submited for building.
     */
    private final Integer submitMonth;

    /**
     * The quarter when the build was submited for building.
     */
    private final Integer submitQuarter;

    /**
     * The status of the build.
     */
    private final String status;

    /**
     * Whether the build is temporary.
     */
    private final Boolean temporarybuild;

    /**
     * Whether the build has used autoalignment.
     */
    private final Boolean autoalign;

    /**
     * Whether the build has used brew pull.
     */
    private final Boolean brewpullactive;

    /**
     * The type of the build.
     */
    private final String buildType;

    /**
     * The root name of the build.
     */
    private final String executionRootName;

    /**
     * The version of the build.
     */
    private final String executionRootVersion;

    /**
     * The ID of the user who triggered the build.
     */
    private final Integer userId;

    /**
     * The username of the user who triggered the build.
     */
    private final String username;

    /**
     * The build configuration ID of the build.
     */
    private final Integer buildConfigurationId;

    /**
     * The build configuration revision of the build.
     */
    private final Integer buildConfigurationRev;

    /**
     * The build configuration name of the build.
     */
    private final String buildConfigurationName;

    /**
     * The build group ID of the build.
     */
    private final Long buildConfigSetRecordId;

    /**
     * The product milestone ID of the build.
     */
    private final Integer productMilestoneId;

    /**
     * The product milestone version of the build.
     */
    private final String productMilestoneVersion;

    /**
     * The project ID of the build.
     */
    private final Integer projectId;

    /**
     * The project name of the build.
     */
    private final String projectName;

    /**
     * The product version ID of the build.
     */
    private final Integer productVersionId;

    /**
     * The product version of the build.
     */
    private final String productVersionVersion;

    /**
     * The product ID of the build.
     */
    private final Integer productId;

    /**
     * The product name of the build.
     */
    private final String productName;

}
