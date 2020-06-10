/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.SupportLevel;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.time.Instant;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * Represents a released version of a product. For example, a Beta, GA, or SP release. Each release is associated with a
 * single product milestone.
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductReleaseRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductReleaseRef implements DTOEntity {

    /**
     * ID of the product release.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    /**
     * Release version.
     */
    @PatchSupport({ REPLACE })
    protected final String version;

    /**
     * Rome wasn't built in a day, nor is PNC. This feature will come in near future.
     */
    @PatchSupport({ REPLACE })
    protected final SupportLevel supportLevel;

    /**
     * The time when the release was released.
     */
    @PatchSupport({ REPLACE })
    protected final Instant releaseDate;

    /**
     * A CPE (Common Platform Enumeration) is a Red Hat identifier assigned to a particular product, product version or
     * product release. A product's CPE identifier is publicly used and can be found in numerous places to identify
     * content. CPEs are used to map packages that are security-relevant and delivered via security errata back to
     * products and by the CVE Engine to map errata to containers when grading.
     */
    @PatchSupport({ REPLACE })
    protected final String commonPlatformEnumeration;

    /**
     * Code given by the Product Pages to the release.
     */
    @PatchSupport({ REPLACE })
    protected final String productPagesCode;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
