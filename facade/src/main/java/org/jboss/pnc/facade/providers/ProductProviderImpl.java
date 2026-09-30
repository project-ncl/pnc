/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withAbbrev;
import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withName;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.validation.groups.ValidationGroup;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.providers.api.ProductProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.ProductMapper;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;

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
                productId = Integer.valueOf(productRest.getId());
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
