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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.Instant;
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
