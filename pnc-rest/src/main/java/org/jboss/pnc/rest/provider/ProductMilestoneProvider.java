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

import static org.jboss.pnc.datastore.predicates.ProductMilestonePredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Preconditions;

@Stateless
public class ProductMilestoneProvider {

    private ProductMilestoneRepository productMilestoneRepository;

    private ProductVersionRepository productVersionRepository;

    @Inject
    public ProductMilestoneProvider(ProductMilestoneRepository productMilestoneRepository,
            ProductVersionRepository productVersionRepository) {

        this.productMilestoneRepository = productMilestoneRepository;
        this.productVersionRepository = productVersionRepository;
    }

    // needed for EJB/CDI
    public ProductMilestoneProvider() {
    }

    public List<ProductMilestoneRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductMilestone.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        Iterable<ProductMilestone> productMilestones = productMilestoneRepository.findAll(filteringCriteria.get(), paging);
        return nullableStreamOf(productMilestones).map(toRestModel()).collect(Collectors.toList());
    }

    public List<ProductMilestoneRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer versionId) {

        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductMilestone.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return mapToListOfProductMilestoneRest(productMilestoneRepository.findAll(
                withProductVersionId(versionId).and(filteringCriteria.get()), paging));
    }

    public ProductMilestoneRest getSpecific(Integer productMilestoneId) {
        ProductMilestone productMilestone = productMilestoneRepository.findOne(productMilestoneId);
        if (productMilestone != null) {
            return new ProductMilestoneRest(productMilestone);
        }
        return null;
    }

    public void update(Integer id, ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productMilestoneRest.getId() == null || productMilestoneRest.getId().equals(id),
                "Entity id does not match the id to update");
        Preconditions.checkArgument(productMilestoneRest.getProductVersionId() != null, "ProductVersion must not be null");
        productMilestoneRest.setId(id);
        ProductVersion productVersion = productVersionRepository.findOne(productMilestoneRest.getProductVersionId());
        ProductMilestone productMilestone = productMilestoneRepository.findOne(productMilestoneRest.getId());
        Preconditions.checkArgument(productMilestone != null,
                "Couldn't find Product Milestone with id " + productMilestoneRest.getId());
        Preconditions.checkArgument(productVersion != null,
                "Couldn't find Product Version with id " + productMilestoneRest.getProductVersionId());
        productMilestoneRepository.save(productMilestoneRest.toProductMilestone(productVersion));
    }

    private Function<ProductMilestone, ProductMilestoneRest> toRestModel() {
        return productMilestone -> new ProductMilestoneRest(productMilestone);
    }

    private List<ProductMilestoneRest> mapToListOfProductMilestoneRest(Iterable<ProductMilestone> entries) {
        return nullableStreamOf(entries).map(toRestModel()).collect(Collectors.toList());
    }

    public Integer store(Integer productVersionId, ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(productMilestoneRest.getId() == null, "Id must be null");
        ProductVersion productVersion = productVersionRepository.findOne(productVersionId);
        Preconditions.checkArgument(productVersion != null, "Couldn't find product version with id " + productVersionId);

        ProductMilestone productMilestone = productMilestoneRepository.save(productMilestoneRest
                .toProductMilestone(productVersion));
        return productMilestone.getId();
    }

}
