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

import static org.jboss.pnc.datastore.predicates.ProductReleasePredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Preconditions;

@Stateless
public class ProductReleaseProvider {

    private ProductReleaseRepository productReleaseRepository;
    private ProductVersionRepository productVersionRepository;

    @Inject
    public ProductReleaseProvider(ProductReleaseRepository productReleaseRepository,
            ProductVersionRepository productVersionRepository) {
        this.productReleaseRepository = productReleaseRepository;
        this.productVersionRepository = productVersionRepository;
    }

    // needed for EJB/CDI
    public ProductReleaseProvider() {
    }

    public List<ProductReleaseRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductRelease.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        Iterable<ProductRelease> productReleases = productReleaseRepository.findAll(filteringCriteria.get(), paging);
        return nullableStreamOf(productReleases).map(toRestModel()).collect(Collectors.toList());
    }

    public List<ProductReleaseRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer versionId) {

        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductRelease.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return mapToListOfProductReleaseRest(productReleaseRepository.findAll(
                withProductVersionId(versionId).and(filteringCriteria.get()), paging));
    }

    public ProductReleaseRest getSpecific(Integer productReleaseId) {
        ProductRelease productRelease = productReleaseRepository.findOne(productReleaseId);
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
        ProductVersion productVersion = productVersionRepository.findOne(productReleaseRest.getProductVersionId());
        ProductRelease productRelease = productReleaseRepository.findOne(productReleaseRest.getId());
        Preconditions.checkArgument(productRelease != null,
                "Couldn't find Product Release with id " + productReleaseRest.getId());
        Preconditions.checkArgument(productVersion != null,
                "Couldn't find Product Version with id " + productReleaseRest.getProductVersionId());

        productReleaseRepository.save(productReleaseRest.toProductRelease(productVersion));
    }

    private Function<ProductRelease, ProductReleaseRest> toRestModel() {
        return productRelease -> new ProductReleaseRest(productRelease);
    }

    private List<ProductReleaseRest> mapToListOfProductReleaseRest(Iterable<ProductRelease> entries) {
        return nullableStreamOf(entries).map(toRestModel()).collect(Collectors.toList());
    }

    public Integer store(Integer productVersionId, ProductReleaseRest productReleaseRest) {
        Preconditions.checkArgument(productReleaseRest.getId() == null, "Id must be null");
        ProductVersion productVersion = productVersionRepository.findOne(productVersionId);
        Preconditions.checkArgument(productVersion != null, "Couldn't find product version with id " + productVersionId);

        ProductRelease productRelease = productReleaseRepository.save(productReleaseRest.toProductRelease(productVersion));
        return productRelease.getId();
    }

}
