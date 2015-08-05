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
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
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
import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.withProductVersionId;

@Stateless
public class ProductMilestoneProvider {

    private ProductMilestoneRepository productMilestoneRepository;

    private ProductVersionRepository productVersionRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public ProductMilestoneProvider(ProductMilestoneRepository productMilestoneRepository,
            ProductVersionRepository productVersionRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.productMilestoneRepository = productMilestoneRepository;
        this.productVersionRepository = productVersionRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    // needed for EJB/CDI
    public ProductMilestoneProvider() {
    }

    public List<ProductMilestoneRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<ProductMilestone> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductMilestone.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productMilestoneRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<ProductMilestoneRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer versionId) {
        Predicate<ProductMilestone> rsqlPredicate = rsqlPredicateProducer.getPredicate(ProductMilestone.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(productMilestoneRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withProductVersionId(versionId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductMilestoneRest getSpecific(Integer productMilestoneId) {
        ProductMilestone productMilestone = productMilestoneRepository.queryById(productMilestoneId);
        if (productMilestone != null) {
            return new ProductMilestoneRest(productMilestone);
        }
        return null;
    }

    public void update(Integer id, ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productMilestoneRest.getId() == null || productMilestoneRest.getId().equals(id),
                "Entity id does not match the id to update");
        productMilestoneRest.setId(id);
        ProductMilestone productMilestone = productMilestoneRepository.queryById(productMilestoneRest.getId());
        Preconditions.checkArgument(productMilestone != null,
                "Couldn't find Product Milestone with id " + productMilestoneRest.getId());
        productMilestoneRepository.save(productMilestoneRest.mergeProductMilestone(productMilestone));
    }

    private Function<ProductMilestone, ProductMilestoneRest> toRestModel() {
        return productMilestone -> new ProductMilestoneRest(productMilestone);
    }

    public Integer store(Integer productVersionId, ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(productMilestoneRest.getId() == null, "Id must be null");
        Preconditions.checkArgument(productMilestoneRest.getProductVersionId() != null, "productVersionId must not be null");
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        Preconditions.checkArgument(productVersion != null, "Couldn't find product version with id " + productVersionId);

        ProductMilestone productMilestone = productMilestoneRepository.save(productMilestoneRest
                .toProductMilestone(productVersion));
        return productMilestone.getId();
    }

}
