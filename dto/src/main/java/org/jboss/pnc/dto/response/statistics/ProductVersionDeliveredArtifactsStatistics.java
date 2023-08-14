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
 * Statistics about the delivered artifacts of a product version.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductVersionDeliveredArtifactsStatistics {

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of this
     * Version.
     */
    long thisVersion;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of
     * other Versions of the same Product.
     */
    long otherVersions;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds contained in Milestones of other
     * Products.
     */
    long otherProducts;

    /**
     * Number of Delivered Artifacts of Milestones of this Version produced by Builds not contained in any Milestone.
     */
    long noMilestone;

    /**
     * Number of Delivered Artifacts of Milestones of this Version not produced in any Build.
     */
    long noBuild;
}
