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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactPK;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import javax.persistence.Tuple;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * Interface for manipulating with {@link DeliverableArtifact} entity
 */
public interface DeliverableArtifactRepository extends Repository<DeliverableArtifact, DeliverableArtifactPK> {

    /**
     * Number of artifacts delivered in product milestone given by id, **which were also built in that milestone**.
     */
    long countMilestoneDeliveredArtifactsBuiltInThisMilestone(Integer productMilestoneId);

    /**
     * Number of artifacts delivered in product milestone given by id, **which were built in another milestone (but same
     * product)**.
     */
    long countMilestoneDeliveredArtifactsBuiltInOtherMilestones(Integer productMilestoneId);

    /**
     * Number of artifacts delivered in product milestone given by id, **which were built in any milestone of different
     * product**.
     */
    long countMilestoneDeliveredArtifactsBuiltByOtherProducts(Integer productMilestoneId);

    /**
     * Number of artifacts delivered in product milestone given by id, **whose build does not belong to any milestone**.
     */
    long countMilestoneDeliveredArtifactsBuiltInNoMilestone(Integer productMilestoneId);

    /**
     * Number of artifacts delivered in product milestone given by id, **which were not built, i.e. were imported**.
     */
    long countMilestoneDeliveredArtifactsNotBuilt(Integer productMilestoneId);

    /**
     * Artifact qualities of **all delivered artifacts in the milestone given by id**.
     */
    EnumMap<ArtifactQuality, Long> getArtifactQualitiesCounts(Integer productMilestoneId);

    /**
     * Repository types of **all delivered artifacts in the milestone given by id**.
     */
    EnumMap<RepositoryType, Long> getRepositoryTypesCounts(Integer productMilestoneId);

    /**
     * Number of product dependencies of artifacts delivered in any milestone assigned to product version given by id.
     * Note: Product dependency = any of our delivered artifacts was built in any milestone under this product.
     */
    long countVersionDeliveredArtifactsProductDependencies(Integer productVersionId);

    /**
     * Number of milestone dependencies of artifacts delivered in any milestone assigned to product version given by id.
     * Note: Milestone dependency = any of our delivered artifacts was built this milestone. Note 2: Also milestones
     * assigned to the product version given by id are counted to this number.
     */
    long countVersionDeliveredArtifactsMilestoneDependencies(Integer productVersionId);

    /**
     * Number of artifacts delivered in any product milestone from product version given by id, **which were also built
     * in one of these milestones**.
     */
    long countVersionDeliveredArtifactsBuiltInThisVersion(Integer productVersionId);

    /**
     * Number of artifacts delivered in any product milestone from product version given by id, **which were built in
     * any milestone of another product version (but same product)**.
     */
    long countVersionDeliveredArtifactsBuiltInOtherVersions(Integer productVersionId);

    /**
     * Number of artifacts delivered in any product milestone from product version given by id, **which were built in
     * any milestone of another product**.
     */
    long countVersionDeliveredArtifactsBuiltByOtherProducts(Integer productVersionId);

    /**
     * Number of artifacts delivered in any product milestone from product version given by id, **whose build does not
     * belong to any milestone**.
     */
    long countVersionDeliveredArtifactsBuiltInNoMilestone(Integer productVersionId);

    /**
     * Number of artifacts delivered in any product milestone from product version given by id, **which were not built,
     * i.e. were imported**.
     */
    long countVersionDeliveredArtifactsNotBuilt(Integer productVersionId);

    // TODO
    List<Tuple> getArtifactQualityStatistics(Set<Integer> ids);

    // TODO
    List<Tuple> getRepositoryTypesStatistics(Set<Integer> ids);
}
