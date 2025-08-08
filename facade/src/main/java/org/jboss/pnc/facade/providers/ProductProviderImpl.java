/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.validation.groups.ValidationGroup;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.util.IdMapperHelper;
import org.jboss.pnc.mapper.api.ProductMapper;
import org.jboss.pnc.facade.providers.api.ProductProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withAbbrev;
import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withName;

@PermitAll
@Stateless
public class ProductProviderImpl
        extends AbstractUpdatableProvider<Integer, org.jboss.pnc.model.Product, Product, ProductRef>
        implements ProductProvider {

    @Inject
    public ProductProviderImpl(ProductRepository repository, ProductMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.Product.class);
    }

    @Override
    public void validateBeforeSaving(Product restEntity) {
        super.validateBeforeSaving(restEntity);
        validateIfNotConflicted(restEntity, WhenCreatingNew.class);
    }

    @Override
    public void validateBeforeUpdating(Integer id, Product restEntity) {
        super.validateBeforeUpdating(id, restEntity);
        validateIfNotConflicted(restEntity, WhenUpdating.class);
    }

    /**
     * Not allowed to delete a product
     *
     * @param id
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting products is prohibited!");
    }

    @SuppressWarnings("unchecked")
    private void validateIfNotConflicted(Product productRest, Class<? extends ValidationGroup> group)
            throws ConflictedEntryException {

        ValidationBuilder.validateObject(productRest, WhenCreatingNew.class).validateConflict(() -> {

            org.jboss.pnc.model.Product product = repository.queryByPredicates(withName(productRest.getName()));

            Integer productId = null;

            if (productRest.getId() != null) {
                productId = IdMapperHelper.toEntity(mapper.getIdMapper(), productRest.getId());
            }

            if (product != null && !product.getId().equals(productId)) {
                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        product.getId(),
                        org.jboss.pnc.model.Product.class,
                        "Product with the same name already exists");
            }

            product = repository.queryByPredicates(withAbbrev(productRest.getAbbreviation()));

            if (product != null && !product.getId().equals(productId)) {
                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        product.getId(),
                        org.jboss.pnc.model.Product.class,
                        "Product with the same abbreviation already exists");
            }

            return null;
        });
    }

}
