/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneArtifactQualityStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneRepositoryTypeStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionStatistics;

public interface ProductVersionProvider
        extends Provider<Integer, org.jboss.pnc.model.ProductVersion, ProductVersion, ProductVersionRef> {

    Page<ProductVersion> getAllForProduct(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productId);

    ProductVersionStatistics getStatistics(String id);

    Page<ProductMilestoneArtifactQualityStatistics> getArtifactQualitiesStatistics(
            int pageIndex,
            int pageSize,
            String sort,
            String query,
            String id);

    Page<ProductMilestoneRepositoryTypeStatistics> getRepositoryTypesStatistics(
            int pageIndex,
            int pageSize,
            String sort,
            String query,
            String id);
}
