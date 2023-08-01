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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Statistics about the delivered artifacts of a milestone.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductMilestoneDeliveredArtifactsStatistics {

    /**
     * Number of delivered artifacts produced by builds in this milestone.
     */
    long thisMilestone;

    /**
     * Number of delivered artifacts produced by builds contained in other milestones of the same product.
     */
    long otherMilestones;

    /**
     * Number of delivered artifacts produced by builds contained in milestones of other products.
     */
    long otherProducts;

    /**
     * Number of delivered artifacts produced by builds not contained in any milestone.
     */
    long noMilestone;

    /**
     * Number of delivered artifacts not produced in any build.
     */
    long noBuild;
}
