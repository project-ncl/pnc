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

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductProviderTest extends AbstractIntIdProviderTest<Product> {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductProviderImpl provider;

    private Product productMock = prepareNewProduct("amazon");
    private Product productMockSecond = prepareNewProduct("nananana");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<Product, Integer> repository() {
        return repository;
    }

    @Before
    public void setup() {
        List<Product> products = new ArrayList<>();

        products.add(productMock);
        products.add(productMockSecond);
        products.add(prepareNewProduct("facebook"));
        products.add(prepareNewProduct("apple"));
        products.add(prepareNewProduct("google"));

        fillRepository(products);
    }

    @Test
    public void testStoreNewProductWithoutId() {

        // when
        org.jboss.pnc.dto.Product productDTO = org.jboss.pnc.dto.Product.builder()
                .name("dustin")
                .description("hello")
                .abbreviation("dustin-the-great")
                .build();

        org.jboss.pnc.dto.Product productDTOSaved = provider.store(productDTO);

        // then
        assertThat(productDTOSaved.getId()).isNotNull().isNotBlank();
        // check if DTO pre-save is the same as DTO post-save
        assertThat(productDTOSaved.getName()).isEqualTo(productDTO.getName());
        assertThat(productDTOSaved.getDescription()).isEqualTo(productDTO.getDescription());
        assertThat(productDTOSaved.getAbbreviation()).isEqualTo(productDTO.getAbbreviation());
    }

    @Test
    public void testStoreNewProductWithIdShouldFail() {

        // when
        org.jboss.pnc.dto.Product productDTO = org.jboss.pnc.dto.Product.builder()
                .id(Integer.toString(entityId.getAndIncrement()))
                .name("dustin-the-second")
                .abbreviation("heya")
                .build();

        // then: can't store new product with id already set
        assertThatThrownBy(() -> provider.store(productDTO)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreProductWithExistingNameShouldFail() {

        // Prepare
        // return entity of product with same name as dto
        when(repository.queryByPredicates(any(Predicate.class))).thenAnswer(env -> productMock);

        // when
        org.jboss.pnc.dto.Product productDTO = org.jboss.pnc.dto.Product.builder()
                .name(productMock.getName())
                .abbreviation("test")
                .build();

        // then
        assertThatThrownBy(() -> provider.store(productDTO)).isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testUpdate() {

        // Prepare
        String newDescription = productMock.getDescription() + "-- Updated";

        org.jboss.pnc.dto.Product productUpdate = org.jboss.pnc.dto.Product.builder()
                .id(productMock.getId().toString())
                .name(productMock.getName())
                .abbreviation(productMock.getAbbreviation())
                .description(newDescription)
                .build();

        // when
        org.jboss.pnc.dto.Product productCheck = provider.update(productMock.getId().toString(), productUpdate);

        // then
        // check if dto pre-update is the same as dto retrieved from database post-update
        assertThat(productCheck.getId()).isEqualTo(productUpdate.getId());
        assertThat(productCheck.getName()).isEqualTo(productUpdate.getName());
        assertThat(productCheck.getDescription()).isEqualTo(productUpdate.getDescription());
    }

    @Test
    public void testUpdateWithDifferentExistingNameShouldFail() {

        // Prepare
        // return entity corresponding to the updated name of dto
        when(repository.queryByPredicates(any(Predicate.class))).thenAnswer(env -> productMockSecond);

        // when
        org.jboss.pnc.dto.Product productUpdate = org.jboss.pnc.dto.Product.builder()
                .id(productMock.getId().toString())
                .name(productMockSecond.getName())
                .abbreviation(productMock.getAbbreviation())
                .description(productMock.getDescription())
                .build();

        assertThatThrownBy(() -> provider.update(productMock.getId().toString(), productUpdate))
                .isInstanceOf(ConflictedEntryException.class);

    }

    @Test
    public void testGetAll() {

        // when
        Page<org.jboss.pnc.dto.Product> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(5);
    }

    @Test
    public void testGetSpecific() {

        // when
        org.jboss.pnc.dto.Product product = provider.getSpecific(productMock.getId().toString());

        // then
        assertThat(product.getId()).isEqualTo(productMock.getId().toString());
        assertThat(product.getName()).isEqualTo(productMock.getName());
    }

    @Test
    public void testDeleteShouldFail() {
        assertThatThrownBy(() -> provider.delete("hello-test")).isInstanceOf(UnsupportedOperationException.class);
    }

    private Product prepareNewProduct(String name) {

        return Product.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .name(name)
                .abbreviation(name)
                .description("WHO ARE YOU")
                .build();
    }
}
