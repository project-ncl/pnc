/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates.withProductId;

@PermitAll
@Stateless
public class ProductVersionProviderImpl
        extends AbstractIntIdProvider<org.jboss.pnc.model.ProductVersion, ProductVersion, ProductVersionRef>
        implements ProductVersionProvider {

    private ProductRepository productRepository;
    private SystemConfig systemConfig;

    @Inject
    public ProductVersionProviderImpl(ProductVersionRepository repository,
                                      ProductVersionMapper mapper,
                                      ProductRepository productRepository,
                                      SystemConfig systemConfig) {

        super(repository, mapper, org.jboss.pnc.model.ProductVersion.class);

        this.productRepository = productRepository;
        this.systemConfig = systemConfig;
    }

    @Override
    public ProductVersion store(ProductVersion restEntity) {

        validateBeforeSaving(restEntity);

        org.jboss.pnc.model.ProductVersion productVersionRestDb = mapper.toEntity(restEntity);

        Product product = productRepository.queryById(Integer.valueOf(restEntity.getProduct().getId()));

        productVersionRestDb.generateBrewTagPrefix(product.getAbbreviation(),
                                                   restEntity.getVersion(),
                                                   systemConfig.getBrewTagPattern());

        repository.save(productVersionRestDb);
        return mapper.toDTO(productVersionRestDb);
    }

    @Override
    public ProductVersion update(String id, ProductVersion restEntity) {
        validateBeforeUpdating(id, restEntity);

        ProductVersion current = super.getSpecific(id);

        if (!current.getVersion().equals(restEntity.getVersion())
                && current.getProductMilestones()
                .stream()
                .anyMatch(milestone -> milestone.getEndDate() != null)) {
            throw new InvalidEntityException("Cannot change version id due to having closed milestone. Product version id: " + id);
        }

        return super.update(id, restEntity);
    }

    @Override
    protected void validateBeforeSaving(ProductVersion restEntity) {

        super.validateBeforeSaving(restEntity);

        Product product = productRepository.queryById(Integer.valueOf(restEntity.getProduct().getId()));

        if (product == null) {
            throw new InvalidEntityException("Product with id: " + restEntity.getProduct().getId() + " does not exist.");
        }
    }


    @Override
    public Page<ProductVersion> getAllForProduct(int pageIndex,
                                                 int pageSize,
                                                 String sortingRsql,
                                                 String query,
                                                 String productId){

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(Integer.valueOf(productId)));
    }
}

