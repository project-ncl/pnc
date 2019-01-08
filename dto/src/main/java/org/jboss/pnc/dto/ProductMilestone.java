/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = ProductMilestone.Builder.class)
public class ProductMilestone extends ProductMilestoneRef {

    @RefHasId(groups = {WhenCreatingNew.class})
    private final ProductVersionRef productVersion;

    private final Set<Integer> performedBuilds;

    private final Set<Integer> distributedArtifactIds;

    private final ProductReleaseRef productRelease;

    @lombok.Builder(builderClassName = "Builder")
    private ProductMilestone(ProductVersionRef productVersion, Set<Integer> performedBuilds, Set<Integer> distributedArtifactIds, ProductReleaseRef productRelease, Integer id, String version, Instant endDate, Instant startingDate, Instant plannedEndDate, String downloadUrl, String issueTrackerUrl) {
        super(id, version, endDate, startingDate, plannedEndDate, downloadUrl, issueTrackerUrl);
        this.productVersion = productVersion;
        this.performedBuilds = performedBuilds;
        this.distributedArtifactIds = distributedArtifactIds;
        this.productRelease = productRelease;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
