/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.SupportLevel;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a released version of a product. For example, a Beta, GA, or SP release. Each release is associated with a
 * single product milestone.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ProductRelease.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRelease extends ProductReleaseRef {

    /**
     * Version of product this is release of.
     */
    @RefHasId(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final ProductVersionRef productVersion;

    /**
     * Milestone that was released.
     */
    private final ProductMilestoneRef productMilestone;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ProductRelease(
            ProductVersionRef productVersion,
            ProductMilestoneRef productMilestone,
            String id,
            String version,
            SupportLevel supportLevel,
            Instant releaseDate,
            String commonPlatformEnumeration,
            String productPagesCode) {
        super(id, version, supportLevel, releaseDate, commonPlatformEnumeration, productPagesCode);
        this.productVersion = productVersion;
        this.productMilestone = productMilestone;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
