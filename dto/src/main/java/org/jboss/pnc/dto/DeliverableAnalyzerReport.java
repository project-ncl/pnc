/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * The report of the deliverable analysis.
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliverableAnalyzerReport implements DTOEntity {

    /**
     * ID of the report (which is in fact the same as the ID of the deliverable analyzer operation by which this report
     * was done).
     */
    String id;

    /**
     * The time when the deliverable analysis was submitted.
     */
    Instant submitTime;

    /**
     * The time when the deliverable analysis was started.
     */
    Instant startTime;

    /**
     * The time when the deliverable analysis finished.
     */
    Instant endTime;

    /**
     * The user who started the analysis.
     */
    User user;

    /**
     * List of artifacts URLs, which were analyzed.
     */
    List<String> urls;

    /**
     * The product milestone on which was the deliverable analysis run (if any).
     */
    ProductMilestoneRef productMilestone;

    /**
     * Set of active labels of this report.
     */
    EnumSet<DeliverableAnalyzerReportLabel> labels;
}
