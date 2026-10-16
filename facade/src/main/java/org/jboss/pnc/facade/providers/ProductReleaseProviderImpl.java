/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.spi.datastore.predicates.ProductReleasePredicates.withProductVersionId;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.mapper.api.ProductReleaseMapper;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;

@PermitAll
@Stateless
public class ProductReleaseProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.ProductRelease, ProductRelease, ProductReleaseRef>
        implements ProductReleaseProvider {

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
                withProductVersionId(Integer.valueOf(productVersionId)));
    }
}
