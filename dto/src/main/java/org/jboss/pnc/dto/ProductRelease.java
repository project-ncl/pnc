/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.SupportLevel;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = ProductRelease.Builder.class)
public class ProductRelease extends ProductReleaseRef {

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final ProductVersionRef productVersion;

    private final ProductMilestoneRef productMilestone;

    @lombok.Builder(builderClassName = "Builder")
    private ProductRelease(ProductVersionRef productVersion, ProductMilestoneRef productMilestone, Integer id, String version, SupportLevel supportLevel, Instant releaseDate, String downloadUrl, String issueTrackerUrl) {
        super(id, version, supportLevel, releaseDate, downloadUrl, issueTrackerUrl);
        this.productVersion = productVersion;
        this.productMilestone = productMilestone;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
