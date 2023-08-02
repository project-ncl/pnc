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
 * Statistics about product's versions.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductVersionStatistics {

    /**
     * Number of milestones created in this version.
     */
    long milestones;

    /**
     * Number of Products to which belong Milestones containing Builds which produced Delivered Artifacts of Milestones
     * of this Version.
     * <p>
     * Note: Product linked with this product version is also counted into this number.
     * </p>
     */
    long productDependencies;

    /**
     * Number of Milestones containing Builds which produced Delivered Artifacts of Milestones of this Version.
     * <p>
     * Note: Milestones from this product version are also counted into this number.
     * </p>
     */
    long milestoneDependencies;

    /**
     * Number of Artifacts produced by Builds contained in Milestones of this Version.
     */
    long artifactsInVersion;

    /**
     * Statistics about this version's delivered artifacts.
     */
    ProductVersionDeliveredArtifactsStatistics deliveredArtifactsSource;
}
