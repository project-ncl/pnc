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
