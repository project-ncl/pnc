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

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import javax.persistence.Tuple;
import java.util.List;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.ProductMilestone} entity.
 */
public interface ProductMilestoneRepository extends Repository<ProductMilestone, Integer> {

    long countBuiltArtifactsInMilestone(Integer id);

    /**
     * Fetches all Delivered Artifacts delivered in at least one of the specified Milestones (milestoneIds). Artifacts
     * must be from Maven or NPM and not from SCRATCH or DELETED analysis.
     * 
     * Returned tuple format: 0) Artifact ID (Integer); 1) Artifact deploy path (String); 2) Artifact repository type
     * (RepositoryType); 3+) (for each Milestone) Whether an Artifact was delivered in a Milestone (Boolean)
     */
    List<Tuple> getArtifactsDeliveredInMilestones(List<Integer> milestoneIds);

    /**
     * Fetches Milestones sharing common Delivered Artifacts with the specified Milestone (milestoneId).
     *
     * Returned tuple format: 0) Milestone ID (Integer); 1) Count of shared Delivered Artifacts (Integer)
     */
    List<Tuple> getMilestonesSharingDeliveredArtifacts(Integer milestoneId);
}
