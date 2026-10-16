/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.List;

import javax.enterprise.event.ObservesAsync;

import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.spi.events.OperationChangedEvent;

public interface ProductMilestoneProvider
        extends Provider<Integer, org.jboss.pnc.model.ProductMilestone, ProductMilestone, ProductMilestoneRef> {

    void closeMilestone(String id, boolean skipPush);

    void observeEvent(@ObservesAsync OperationChangedEvent event);

    void cancelMilestoneCloseProcess(String id) throws RepositoryViolationException, EmptyEntityException;

    Page<ProductMilestone> getProductMilestonesForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId);

    Page<MilestoneInfo> getMilestonesOfArtifact(
            String id,
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String queryRsql);

    ValidationResponse validateVersion(String productVersionId, String version);

    ProductMilestoneStatistics getStatistics(String id);

    List<DeliveredArtifactInMilestones> getArtifactsDeliveredInMilestonesGroupedByPrefix(List<String> milestoneIds);

    Graph<ProductMilestone> getMilestonesSharingDeliveredArtifactsGraph(String milestoneId, Integer depthLimit);
}
