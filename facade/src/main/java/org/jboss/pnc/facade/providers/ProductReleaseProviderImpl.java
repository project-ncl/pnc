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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.util.IdMapperHelper;
import org.jboss.pnc.mapper.api.ProductReleaseMapper;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.ProductReleasePredicates.withProductVersionId;

@PermitAll
@Stateless
public class ProductReleaseProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.ProductRelease, ProductRelease, ProductReleaseRef>
        implements ProductReleaseProvider {

    @Inject
    private ProductVersionMapper productVersionMapper;

    @Inject
    public ProductReleaseProviderImpl(ProductReleaseRepository repository, ProductReleaseMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.ProductRelease.class);
    }

    @Override
    public Page<ProductRelease> getProductReleasesForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withProductVersionId(IdMapperHelper.toEntity(productVersionMapper.getIdMapper(), productVersionId)));
    }
}
