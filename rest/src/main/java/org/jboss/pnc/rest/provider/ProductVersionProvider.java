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

import com.google.common.base.Preconditions;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.spi.datastore.repositories.*;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductVersionId;

@Stateless
public class ProductVersionProvider {

    private ProductVersionRepository productVersionRepository;

    private ProductRepository productRepository;

    private ProductMilestoneRepository productMilestoneRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public ProductVersionProvider(ProductVersionRepository productVersionRepository, ProductRepository productRepository, ProductMilestoneRepository productMilestoneRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.productVersionRepository = productVersionRepository;
        this.productRepository = productRepository;
        this.productMilestoneRepository = productMilestoneRepository;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
    }

    // needed for EJB/CDI
    public ProductVersionProvider() {
    }

    public List<ProductVersionRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<ProductVersion> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductVersion.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productVersionRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<ProductVersionRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query, Integer productId){
        Predicate<ProductVersion> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductVersion.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productVersionRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withProductId(productId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductVersionRest getSpecific(Integer productVersionId) {
        List<ProductVersion> productVersions = productVersionRepository.queryWithPredicates(withProductVersionId(productVersionId));
        if (productVersions.size() > 0) {
            return new ProductVersionRest(productVersions.get(0));
        }
        return null;
    }

    public Integer store(Integer productId, ProductVersionRest productVersionRest) {
        Preconditions.checkArgument(productVersionRest.getId() == null, "Id must be null");
        Product product = productRepository.queryById(productId);
        Preconditions.checkArgument(product != null, "Couldn't find product with id " + productId);

        ProductVersion productVersion = productVersionRepository.save(productVersionRest.toProductVersion(product));
        return productVersion.getId();
    }

    public void update(Integer id, Integer productId, ProductVersionRest productVersionRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productVersionRest.getId() == null || productVersionRest.getId().equals(id),
                "Entity id does not match the id to update");
        productVersionRest.setId(id);
        Product product = productRepository.queryById(productId);
        ProductVersion productVersion = productVersionRepository.queryById(productVersionRest.getId());
        Preconditions.checkArgument(productVersion != null,
                "Couldn't find Product Version with id " + productVersionRest.getId());
        Preconditions.checkArgument(product != null,
                "Couldn't find Product with id " + product.getId());
        ProductVersion updatedProductVersion = productVersionRest.toProductVersion(productVersion);
        if (productVersionRest.getCurrentProductMilestoneId() != null) {
            ProductMilestone productMilestone = productMilestoneRepository.queryById(productVersionRest.getCurrentProductMilestoneId());
            Preconditions.checkArgument(productMilestone != null,
                "Couldn't find ProductMilestone with id " + productVersionRest.getCurrentProductMilestoneId());
            updatedProductVersion.setCurrentProductMilestone(productMilestone);
        }
        productVersionRepository.save(updatedProductVersion);
    }

    private Function<ProductVersion, ProductVersionRest> toRestModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    private Function<BuildConfigurationSet, BuildConfigurationSetRest> buildConfigSetToRestModel() {
        return buildConfigSet -> new BuildConfigurationSetRest(buildConfigSet);
    }

    public List<BuildConfigurationSetRest> getBuildConfigurationSets(Integer productVersionId) {
        ProductVersion productVersion = productVersionRepository.queryByPredicates(withProductVersionId(productVersionId));
        Set<BuildConfigurationSet> buildConfigSets = productVersion.getBuildConfigurationSets();
        return nullableStreamOf(buildConfigSets)
                .map(buildConfigSetToRestModel())
                .collect(Collectors.toList());
    }

}
