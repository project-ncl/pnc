/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * This is entry describing milestone that produced or consumed an artifact.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = MilestoneInfo.Builder.class)
public class MilestoneInfo {

    /**
     * ID of the product that the milestone belongs to.
     */
    private final String productId;
    /**
     * Name of the product that the milestone belongs to.
     */
    private final String productName;
    /**
     * ID of the product version that the milestone is part of.
     */
    private final String productVersionId;
    /**
     * Version of the product version that the milestone is part of.
     */
    private final String productVersionVersion;
    /**
     * ID of the milestone.
     */
    private final String milestoneId;
    /**
     * Version of the milestone.
     */
    private final String milestoneVersion;
    /**
     * Date and time when the milestone was closed.
     */
    private final Instant milestoneEndDate;
    /**
     * ID of the release of the milestone.
     */
    private final String releaseId;
    /**
     * Version of the release of the milestone.
     */
    private final String releaseVersion;
    /**
     * Date and time when the milestone was released.
     */
    private final Instant releaseReleaseDate;
    /**
     * Whether the queried artifact was built in this milestone or not.
     */
    private final boolean built;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
