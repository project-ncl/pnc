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
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;


@Stateless
public class ProductVersionProvider extends AbstractProvider<ProductVersion, ProductVersionRest> {

    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    
    private ProductRepository productRepository;

    @Inject
    public ProductVersionProvider(ProductVersionRepository productVersionRepository, BuildConfigurationSetRepository buildConfigurationSetRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer,
            ProductRepository productRepository) {
        super(productVersionRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.productRepository = productRepository;
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

    public void updateBuildConfigurationSets(Integer id, List<BuildConfigurationSetRest> bcsRestModels) throws ValidationException {
        if (bcsRestModels == null) {
            throw new InvalidEntityException("No BuildConfigurationSets supplied");
        }

        ProductVersion productVersion = repository.queryById(id);

        Set<BuildConfigurationSet> addedDbModels = new HashSet<>();

        for (BuildConfigurationSetRest restModel : bcsRestModels) {
            BuildConfigurationSet dbModel = buildConfigurationSetRepository.queryById(restModel.getId());

            if (dbModel == null) {
                throw new InvalidEntityException("Invalid BuildConfigurationSet");
            }

            if (dbModel.getProductVersion() != null && !dbModel.getProductVersion().getId().equals(productVersion.getId())) {
                throw new ConflictedEntryException(format("BuildConfigurationSet: '%s' is already associated with a different Product Version",
                        dbModel.getName()), BuildConfigurationSet.class, dbModel.getId());
            }

            addedDbModels.add(dbModel);
        }

        Set<BuildConfigurationSet> removedDbModels = Sets.difference(productVersion.getBuildConfigurationSets(), addedDbModels);

        productVersion.setBuildConfigurationSets(addedDbModels);
        validateBeforeUpdating(id, new ProductVersionRest(productVersion));

        removedDbModels.forEach(x -> {
            x.setProductVersion(null);
            buildConfigurationSetRepository.save(x);
        });
        addedDbModels.forEach(x -> {
            x.setProductVersion(productVersion);
            buildConfigurationSetRepository.save(x);
        });
        repository.save(productVersion);
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
    public Integer store(ProductVersionRest restEntity) throws ValidationException {
        validateBeforeSaving(restEntity);
        ProductVersion.Builder productVersionBuilder = restEntity.toDBEntityBuilder();
        Product product = productRepository.queryById(restEntity.getProductId());
        productVersionBuilder.generateBrewTagPrefix(product.getAbbreviation(), restEntity.getVersion());
        
        return repository.save(productVersionBuilder.build()).getId();
    }

    @Override
    protected void validateBeforeSaving(ProductVersionRest restEntity) throws ValidationException {
        super.validateBeforeSaving(restEntity);
        Product product = productRepository.queryById(restEntity.getProductId());
        if (product == null) {
            throw new InvalidEntityException("Product with id: " + restEntity.getProductId() + " does not exist.");
        }
    }

    private void validateBeforeUpdate(Integer productVersionId, Set<BuildConfigurationSet> sets) throws InvalidEntityException {
        for (BuildConfigurationSet set : sets) {
            validateBeforeUpdate(productVersionId, sets);
        }
    }

    private void validateBeforeUpdate(Integer productVersionId, BuildConfigurationSet set) throws InvalidEntityException {
        BuildConfigurationSetRest restModel = new BuildConfigurationSetRest(set);
        ValidationBuilder.validateObject(set, WhenUpdating.class).validateCondition(
                set != null, "Invalid BuildConfigurationSet"
        );

        ValidationBuilder.validateObject(restModel, WhenUpdating.class).validateCondition(
                set.getProductVersion() == null || set.getProductVersion().getId().equals(productVersionId),
                format("BuildConfigurationSet: '%s' is already associated with a different Product Version", restModel.getName()));
    }

}
