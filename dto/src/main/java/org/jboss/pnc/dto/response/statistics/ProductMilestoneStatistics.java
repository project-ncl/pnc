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
package org.jboss.pnc.dto.response.statistics;

import lombok.Builder;
import lombok.Value;
import org.jboss.pnc.api.enums.ArtifactQuality;
import org.jboss.pnc.api.enums.RepositoryType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Statistics about product's milestones.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
public class ProductMilestoneStatistics {

    /**
     * Number of artifacts produced by builds contained in this milestone.
     */
    Integer artifactsInMilestone;

    /**
     * Statistics about this milestone's delivered artifacts.
     */
    DeliveredArtifactsStatistics deliveredArtifactsSource;

    /**
     * Proportion of quality of Delivered Artifacts.
     */
    EnumMap<ArtifactQuality, Integer> artifactQuality;

    /**
     * Proportion of repository type of Delivered Artifacts.
     */
    EnumMap<RepositoryType, Integer> repositoryType;
}
