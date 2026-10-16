/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Statistics about the delivered artifacts of a milestone.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductMilestoneDeliveredArtifactsStatistics {

    /**
     * Number of delivered artifacts produced by builds in this milestone.
     */
    long thisMilestone;

    /**
     * Number of delivered artifacts produced by builds contained in other milestones of the same product.
     */
    long otherMilestones;

    /**
     * Number of delivered artifacts produced by builds contained in milestones of other products.
     */
    long otherProducts;

    /**
     * Number of delivered artifacts produced by builds not contained in any milestone.
     */
    long noMilestone;

    /**
     * Number of delivered artifacts not produced in any build.
     */
    long noBuild;
}
