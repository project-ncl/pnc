/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.util.Map;

import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A product is a deliverable package composed of multiple project.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Product.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product extends ProductRef {

    /**
     * List of this product's versions.
     */
    @PatchSupport({ ADD, REPLACE })
    private final Map<String, ProductVersionRef> productVersions;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Product(
            Map<String, ProductVersionRef> productVersions,
            String id,
            String name,
            String description,
            String abbreviation,
            String productManagers,
            String productPagesCode) {
        super(id, name, description, abbreviation, productManagers, productPagesCode);
        this.productVersions = productVersions;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
