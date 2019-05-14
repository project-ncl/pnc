/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProductReleasePredicates.withProductVersionId;

@PermitAll
@Stateless
public class ProductReleaseProvider extends AbstractProvider<ProductRelease, ProductReleaseRest> {

    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    public ProductReleaseProvider(ProductReleaseRepository productReleaseRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer,
            ProductMilestoneRepository productMilestoneRepository) {
        super(productReleaseRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.productMilestoneRepository = productMilestoneRepository;
    }

    // needed for EJB/CDI
    public ProductReleaseProvider() {
    }

    @Override
    protected Function<? super ProductRelease, ? extends ProductReleaseRest> toRESTModel() {
        return productRelease -> new ProductReleaseRest(productRelease);
    }

    @Override
    protected Function<? super ProductReleaseRest, ? extends ProductRelease> toDBModel() {
        return productReleaseRest ->  productReleaseRest.toDBEntityBuilder().build();
    }

    public CollectionInfo<ProductReleaseRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer versionId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(versionId));
    }

}
