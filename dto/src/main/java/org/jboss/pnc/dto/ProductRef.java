/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * A product is a deliverable package composed of multiple project.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRef implements DTOEntity {

    /**
     * ID of the product.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Product name.
     */
    @PatchSupport({ REPLACE })
    @NotBlank(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String name;

    /**
     * Product description.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String description;

    /**
     * Product abbreviation.
     *
     * @see Patterns#PRODUCT_ABBREVIATION
     */
    @PatchSupport({ REPLACE })
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @Pattern(regexp = Patterns.PRODUCT_ABBREVIATION, groups = { WhenCreatingNew.class, WhenUpdating.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String abbreviation;

    /**
     * Comma separated list of product managers.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String productManagers;

    /**
     * The code given to the product by Product Pages.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String productPagesCode;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
