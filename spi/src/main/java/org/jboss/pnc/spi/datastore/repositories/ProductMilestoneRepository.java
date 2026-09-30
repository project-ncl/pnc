/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.List;

import javax.persistence.Tuple;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

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
