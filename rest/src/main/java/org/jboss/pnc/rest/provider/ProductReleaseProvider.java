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
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.ProductReleasePredicates.withProductVersionId;

@Stateless
public class ProductReleaseProvider {

    private ProductReleaseRepository productReleaseRepository;

    private ProductVersionRepository productVersionRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public ProductReleaseProvider(ProductReleaseRepository productReleaseRepository,
            ProductVersionRepository productVersionRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.productReleaseRepository = productReleaseRepository;
        this.productVersionRepository = productVersionRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    // needed for EJB/CDI
    public ProductReleaseProvider() {
    }

    public List<ProductReleaseRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<ProductRelease> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductRelease.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productReleaseRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<ProductReleaseRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer versionId) {
        Predicate<ProductRelease> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductRelease.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productReleaseRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withProductVersionId(versionId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductReleaseRest getSpecific(Integer productReleaseId) {
        ProductRelease productRelease = productReleaseRepository.queryById(productReleaseId);
        if (productRelease != null) {
            return new ProductReleaseRest(productRelease);
        }
        return null;
    }

    public void update(Integer id, ProductReleaseRest productReleaseRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productReleaseRest.getId() == null || productReleaseRest.getId().equals(id),
                "Entity id does not match the id to update");
        productReleaseRest.setId(id);
        ProductVersion productVersion = productVersionRepository.queryById(productReleaseRest.getProductVersionId());
        ProductRelease productRelease = productReleaseRepository.queryById(productReleaseRest.getId());
        Preconditions.checkArgument(productRelease != null,
                "Couldn't find Product Release with id " + productReleaseRest.getId());
        Preconditions.checkArgument(productVersion != null,
                "Couldn't find Product Version with id " + productReleaseRest.getProductVersionId());

        productReleaseRepository.save(productReleaseRest.toProductRelease(productVersion));
    }

    private Function<ProductRelease, ProductReleaseRest> toRestModel() {
        return productRelease -> new ProductReleaseRest(productRelease);
    }

    public Integer store(Integer productVersionId, ProductReleaseRest productReleaseRest) {
        Preconditions.checkArgument(productReleaseRest.getId() == null, "Id must be null");
        Preconditions.checkArgument(productReleaseRest.getProductVersionId() != null, "productVersionId must not be null");
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        Preconditions.checkArgument(productVersion != null, "Couldn't find product version with id " + productVersionId);

        ProductRelease productRelease = productReleaseRepository.save(productReleaseRest.toProductRelease(productVersion));
        return productRelease.getId();
    }

}
