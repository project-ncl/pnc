/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response.statistics;

import java.util.EnumMap;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Statistics about product's milestones.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductMilestoneStatistics {

    /**
     * Number of artifacts produced by builds contained in this milestone.
     */
    long artifactsInMilestone;

    /**
     * Statistics about this milestone's delivered artifacts.
     */
    ProductMilestoneDeliveredArtifactsStatistics deliveredArtifactsSource;

    /**
     * Proportion of quality of Delivered Artifacts.
     */
    EnumMap<ArtifactQuality, Long> artifactQuality;

    /**
     * Proportion of repository type of Delivered Artifacts.
     */
    EnumMap<RepositoryType, Long> repositoryType;
}
