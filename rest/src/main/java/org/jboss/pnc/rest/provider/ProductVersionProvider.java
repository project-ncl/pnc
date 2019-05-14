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

import com.google.common.collect.Sets;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;


@PermitAll
@Stateless
public class ProductVersionProvider extends AbstractProvider<ProductVersion, ProductVersionRest> {

    private Logger logger = LoggerFactory.getLogger(ProductVersionProvider.class);

    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    private ProductRepository productRepository;

    private ProductMilestoneRepository productMilestoneRepository;

    private SystemConfig systemConfig;

    @Inject
    public ProductVersionProvider(
            ProductVersionRepository productVersionRepository,
            BuildConfigurationSetRepository buildConfigurationSetRepository,
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer,
            ProductRepository productRepository,
            ProductMilestoneRepository productMilestoneRepository,
            SystemConfig systemConfig) {
        super(productVersionRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.productRepository = productRepository;
        this.productMilestoneRepository = productMilestoneRepository;
        this.systemConfig = systemConfig;
    }

    // needed for EJB/CDI
    @Deprecated
    public ProductVersionProvider() {
    }

    public CollectionInfo<ProductVersionRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer productId){
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(productId));
    }

    public CollectionInfo<ProductVersionRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer buildConfigurationId){
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(buildConfigurationId));
    }

    public ProductVersion updateBuildConfigurationSets(Integer id, List<BuildConfigurationSetRest> buildConfigurationSetsToAdd) throws
            RestValidationException {
        if (buildConfigurationSetsToAdd == null) {
            throw new InvalidEntityException("No BuildConfigurationSets supplied");
        }

        ProductVersion productVersion = repository.queryById(id);
        List<BuildConfigurationSet> buildConfigurationSets = buildConfigurationSetRepository.withProductVersionId(productVersion.getId());

        if (logger.isTraceEnabled()) {
            logger.trace("Retrieved ProductVersion: {}; Retrieved BuildConfigurationSets: {}",
                    productVersion, buildConfigurationSets.stream().map(bcs -> bcs.getId().toString()).collect(Collectors.joining()));
        }

        Set<BuildConfigurationSet> addedBCSets = new HashSet<>();

        for (BuildConfigurationSetRest configurationSetToAdd : buildConfigurationSetsToAdd) {
            BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(configurationSetToAdd.getId());
            logger.trace("Validating buildConfigurationSet: {}", buildConfigurationSet);

            if (buildConfigurationSet == null) {
                throw new InvalidEntityException("Invalid BuildConfigurationSet");
            }

            if (buildConfigurationSet.getProductVersion() != null
                    && !buildConfigurationSet.getProductVersion().getId().equals(productVersion.getId())) {
                throw new ConflictedEntryException(format("BuildConfigurationSet: '%s' is already associated with a different Product Version",
                        buildConfigurationSet.getName()), BuildConfigurationSet.class, buildConfigurationSet.getId());
            }

            addedBCSets.add(buildConfigurationSet);
        }

        Set<BuildConfigurationSet> removedBCSets = Sets.difference(new HashSet<>(buildConfigurationSets), addedBCSets);

        productVersion.setBuildConfigurationSets(addedBCSets);
        validateBeforeUpdating(id, new ProductVersionRest(productVersion));
        if (logger.isTraceEnabled()) {
            logger.trace("About to remove BCSets from productVersion: {}",
                    removedBCSets.stream().map(set -> set.getId().toString()).collect(Collectors.joining()));
        }
        removedBCSets.forEach(removed -> {
            removed.setProductVersion(null);
            buildConfigurationSetRepository.save(removed);
        });
        if (logger.isTraceEnabled()) {
            logger.trace("About to add BCSets to productVersion: {}",
                    addedBCSets.stream().map(set -> set.getId().toString()).collect(Collectors.joining()));
        }
        addedBCSets.forEach(added -> {
            added.setProductVersion(productVersion);
            productVersion.getBuildConfigurationSets().add(added);
            buildConfigurationSetRepository.save(added);
        });
        if (logger.isTraceEnabled()) {
            logger.trace("About to save ProductVersion: {}; Contains BuildConfigurationSets: {}",
                    productVersion, productVersion.getBuildConfigurationSets().stream().map(bcs -> bcs.getId().toString()).collect(Collectors.joining()));
        }
        return repository.save(productVersion);
    }

    @Override
    protected Function<? super ProductVersion, ? extends ProductVersionRest> toRESTModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    @Override
    protected Function<? super ProductVersionRest, ? extends ProductVersion> toDBModel() {
        return productVersionRest -> productVersionRest.toDBEntityBuilder().build();
    }

    @Override
    public Integer store(ProductVersionRest restEntity) throws RestValidationException {
        validateBeforeSaving(restEntity);
        ProductVersion.Builder productVersionBuilder = restEntity.toDBEntityBuilder();
        Product product = productRepository.queryById(restEntity.getProductId());
        productVersionBuilder.generateBrewTagPrefix(product.getAbbreviation(), restEntity.getVersion(), systemConfig.getBrewTagPattern());

        return repository.save(productVersionBuilder.build()).getId();
    }

    @Override
    protected void validateBeforeSaving(ProductVersionRest restEntity) throws RestValidationException {
        super.validateBeforeSaving(restEntity);
        Product product = productRepository.queryById(restEntity.getProductId());
        if (product == null) {
            throw new InvalidEntityException("Product with id: " + restEntity.getProductId() + " does not exist.");
        }
    }

    @Override
    protected void validateBeforeUpdating(Integer id, ProductVersionRest restEntity) throws RestValidationException {
        super.validateBeforeUpdating(id, restEntity);
        // check if new current product milestone is not closed
        Integer newMilestoneId = restEntity.getCurrentProductMilestoneId();
        if (newMilestoneId != null) {
            ProductVersion currentVersion = repository.queryById(id);
            ProductMilestone currentProductMilestone = currentVersion.getCurrentProductMilestone();
            // only check if the milestone ID is changing
            if (currentProductMilestone == null || !currentProductMilestone.getId().equals(newMilestoneId)) {
                ProductMilestone newMilestone = productMilestoneRepository.queryById(newMilestoneId);
                if (newMilestone == null) {
                    throw new InvalidEntityException("Milestone with id: " + newMilestoneId
                            + " does not exist.");
                } else if (newMilestone.getEndDate() != null) {
                    throw new InvalidEntityException("Milestone with id: " + newMilestoneId
                            + " is closed, so cannot be set as current.");
                }
            }
        }
    }

}
