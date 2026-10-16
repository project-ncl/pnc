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
 * Statistics about the delivered artifacts of a product version.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductVersionDeliveredArtifactsStatistics {

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of this
     * Version.
     */
    long thisVersion;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of other
     * Versions of the same Product.
     */
    long otherVersions;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of other
     * Products.
     */
    long otherProducts;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds not contained in any Milestone.
     */
    long noMilestone;

    /**
     * Number of Delivered Artifacts of Milestones of this Version not produced in any Build.
     */
    long noBuild;
}
