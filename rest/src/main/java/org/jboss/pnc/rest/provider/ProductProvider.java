/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.validation.ConflictedEntryValidator.ConflictedEntryValidationError;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.rest.validation.groups.ValidationGroup;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProductPredicates.withName;

@Stateless
public class ProductProvider extends AbstractProvider<Product, ProductRest> {

    @Inject
    public ProductProvider(ProductRepository productRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(productRepository,rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    // needed for EJB/CDI
    public ProductProvider() {
    }

    @Override
    protected Function<? super Product, ? extends ProductRest> toRESTModel() {
        return ProductRest::new;
    }

    @Override
    protected Function<? super ProductRest, ? extends Product> toDBModel() {
        return product -> product.toDBEntityBuilder().build();
    }

    @Override
    protected void validateBeforeSaving(ProductRest restEntity) throws RestValidationException {
        super.validateBeforeSaving(restEntity);
        validateIfNotConflicted(restEntity, WhenCreatingNew.class);
    }
    @Override
    protected void validateBeforeUpdating(Integer id, ProductRest restEntity) throws RestValidationException {
        super.validateBeforeUpdating(id, restEntity);
        validateIfNotConflicted(restEntity, WhenUpdating.class);
    }

    @SuppressWarnings("unchecked")
    private void validateIfNotConflicted(ProductRest productRest, Class<? extends ValidationGroup> group) throws ConflictedEntryException {
        ValidationBuilder.validateObject(productRest, WhenCreatingNew.class).validateConflict(() -> {
                    Product product = repository.queryByPredicates(withName(productRest.getName()));
                    if (product != null && !product.getId().equals(productRest.getId())) {
                        return new ConflictedEntryValidationError(product.getId(), Product.class, "Product with the same name already exists");
                    }
                    return null;
                }
        );
    }
}
