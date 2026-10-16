/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.util.Map;

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
 * Product version represents one product stream like "6.3", "6,4", "7.1".
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductVersionRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersionRef implements DTOEntity {
    private static final String VERSION_CONSTRAINT = "The version should consist of two numeric parts separated by a dot.";

    /**
     * ID of the product version.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * The major.minor version string. {@value VERSION_CONSTRAINT}
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @Pattern(
            message = VERSION_CONSTRAINT,
            regexp = Patterns.PRODUCT_STREAM_VERSION,
            groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String version;

    /**
     * Map of attributes of the product version.
     */
    @PatchSupport({ ADD, REMOVE, REPLACE })
    protected final Map<String, String> attributes;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
