/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.util.Map;

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Product version represents one product stream like "6.3", "6,4", "7.1".
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ProductVersion.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersion extends ProductVersionRef {

    /**
     * Product that this is a version of.
     */
    @PatchSupport({ REPLACE })
    @RefHasId(groups = { WhenCreatingNew.class })
    private final ProductRef product;

    /**
     * Current milestone of this product version that is being productized.
     */
    @PatchSupport({ REPLACE })
    private final ProductMilestoneRef currentProductMilestone;

    /**
     * List of all milestones in this product version.
     */
    @PatchSupport({ ADD, REPLACE })
    private final Map<String, ProductMilestoneRef> productMilestones;

    /**
     * List of all releases in this product version.
     */
    @PatchSupport({ ADD, REPLACE })
    private final Map<String, ProductReleaseRef> productReleases;

    /**
     * List of all group configs linked to this product version.
     */
    @PatchSupport({ ADD, REPLACE, REMOVE })
    private final Map<String, GroupConfigurationRef> groupConfigs;

    /**
     * List of all group configs linked to this product version.
     */
    @PatchSupport({ ADD, REPLACE, REMOVE })
    private final Map<String, BuildConfigurationRef> buildConfigs;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ProductVersion(
            ProductRef product,
            ProductMilestoneRef currentProductMilestone,
            Map<String, ProductMilestoneRef> productMilestones,
            Map<String, ProductReleaseRef> productReleases,
            Map<String, GroupConfigurationRef> groupConfigs,
            Map<String, BuildConfigurationRef> buildConfigs,
            String id,
            String version,
            Map<String, String> attributes) {
        super(id, version, attributes);
        this.product = product;
        this.currentProductMilestone = currentProductMilestone;
        this.productMilestones = productMilestones;
        this.productReleases = productReleases;
        this.groupConfigs = groupConfigs;
        this.buildConfigs = buildConfigs;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
