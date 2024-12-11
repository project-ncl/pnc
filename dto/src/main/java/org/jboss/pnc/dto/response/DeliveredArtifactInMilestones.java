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
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Delivered Artifact and its versions in Milestones it was delivered in.
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = DeliveredArtifactInMilestones.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveredArtifactInMilestones {

    /**
     * Artifact identifier without the Redhat version part.
     */
    protected final String artifactIdentifierPrefix;

    /**
     * Product Milestone ID mapped to list of Artifact versions delivered in the Milestone with the same identifier
     * prefix.
     */
    protected final Map<String, List<ArtifactVersion>> productMilestoneArtifacts;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
