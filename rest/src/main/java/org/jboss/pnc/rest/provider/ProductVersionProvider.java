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

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;

@Stateless
public class ProductVersionProvider extends AbstractProvider<ProductVersion, ProductVersionRest> {

    private BuildConfigurationSetProvider buildConfigurationSetProvider;

    private ProductRepository productRepository;

    @Inject
    public ProductVersionProvider(ProductVersionRepository productVersionRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer,
            BuildConfigurationSetProvider buildConfigurationSetProvider, ProductRepository productRepository) {
        super(productVersionRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationSetProvider = buildConfigurationSetProvider;
        this.productRepository = productRepository;
    }

    // needed for EJB/CDI
    public ProductVersionProvider() {
    }

    public List<ProductVersionRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query, Integer productId){
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(productId));
    }

    @Override
    protected Function<? super ProductVersion, ? extends ProductVersionRest> toRESTModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    @Override
    protected Function<? super ProductVersionRest, ? extends ProductVersion> toDBModelModel() {
        return productVersion -> {
            if(productVersion.getId() != null) {
                ProductVersion productVersionFromDB = repository.queryById(productVersion.getId());
                return productVersion.toProductVersion(productVersionFromDB);
            }
            Product productFromDB = productRepository.queryById(productVersion.getProductId());
            return productVersion.toProductVersion(productFromDB);
        };
    }

    public List<BuildConfigurationSetRest> getBuildConfigurationSets(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer productVersionId) {
        return buildConfigurationSetProvider.queryForCollection(pageIndex, pageSize, sortingRsql, rsql, BuildConfigurationSetPredicates
                .withProductVersionId(productVersionId));
    }

}
