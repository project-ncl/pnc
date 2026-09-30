/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response.statistics;

import java.util.EnumMap;

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.enums.ArtifactQuality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Statistics about proportion of quality of delivered artifacts.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductMilestoneArtifactQualityStatistics {

    /**
     * Identification of {@link org.jboss.pnc.dto.ProductMilestone} to which the proportion below links to.
     */
    ProductMilestoneRef productMilestone;

    /**
     * Proportion of quality of Delivered Artifacts.
     */
    EnumMap<ArtifactQuality, Long> artifactQuality;
}
