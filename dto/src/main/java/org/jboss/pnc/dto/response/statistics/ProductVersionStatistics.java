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
 * Statistics about product's versions.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductVersionStatistics {

    /**
     * Number of milestones created in this version.
     */
    long milestones;

    /**
     * Number of Products to which belong Milestones containing Builds which produced Delivered Artifacts of Milestones
     * of this Version.
     * <p>
     * Note: The product associated with this product version is also included in this number.
     * </p>
     */
    long productDependencies;

    /**
     * Number of Milestones containing Builds which produced Delivered Artifacts of Milestones of this Version.
     * <p>
     * Note: Milestones from this product version are also included in this number.
     * </p>
     */
    long milestoneDependencies;

    /**
     * Number of Artifacts produced by Builds contained in Milestones of this Version.
     */
    long artifactsInVersion;

    /**
     * Statistics about this version's delivered artifacts.
     */
    ProductVersionDeliveredArtifactsStatistics deliveredArtifactsSource;
}
