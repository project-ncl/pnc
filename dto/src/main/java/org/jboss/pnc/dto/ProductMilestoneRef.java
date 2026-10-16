/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * A milestone represents a stage in the product(ization) process. A single product version, for example "1.0", can be
 * associated with several product milestones such as "1.0.0.build1", "1.0.0.build2", etc. A milestone represents the
 * set of work (build records) that was performed during a development cycle from the previous milestone until the end
 * of the current milestone.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductMilestoneRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductMilestoneRef implements DTOEntity {

    /**
     * ID of the product milestone.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Milestone version.
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @Pattern(
            groups = { WhenCreatingNew.class, WhenUpdating.class },
            regexp = Patterns.PRODUCT_MILESTONE_VERSION,
            message = "Version doesn't match the required pattern " + Patterns.PRODUCT_MILESTONE_VERSION)
    protected final String version;

    /**
     * The time when the work on the milestone ended. If the endDate is set, the milestone is closed and no new content
     * can be added to it.
     */
    @PatchSupport({ REPLACE })
    @Null(groups = WhenCreatingNew.class)
    protected final Instant endDate;

    /**
     * The scheduled starting date of this milestone.
     */
    @PatchSupport({ REPLACE })
    protected final Instant startingDate;

    /**
     * The scheduled ending date of this milestone.
     */
    @PatchSupport({ REPLACE })
    protected final Instant plannedEndDate;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
