/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.assertj.core.api.Condition;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductVersionProviderTest extends AbstractIntIdProviderTest<ProductVersion> {
    @Mock
    private ProductVersionRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMilestoneRepository milestoneRepository;

    @Mock
    private BuildConfigurationSetRepository configurationSetRepository;

    @Mock
    private BuildConfigurationRepository configurationRepository;

    @Mock
    SystemConfig systemConfig;

    @Spy
    @InjectMocks
    protected ProductVersionProviderImpl provider;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<ProductVersion, Integer> repository() {
        return repository;
    }

    private ProductVersion pv = prepareProductVersion("2.0", 1);

    @Before
    public void fill() {
        List<ProductVersion> versions = new ArrayList<>();
        versions.add(
                prepareProductVersion(
                        "1.0",
                        2,
                        prepareProductMilestone(1, Date.from(Instant.now().plus(1L, ChronoUnit.DAYS)))));
        versions.add(prepareProductVersion("3.0", 2));
        versions.add(pv);
        fillRepository(versions);
    }

    @Test
    public void testGetSpecific() {
        org.jboss.pnc.dto.ProductVersion buildConfiguration = provider.getSpecific(pv.getId().toString());

        assertThat(buildConfiguration).isNotNull();
        assertThat(buildConfiguration.getId()).isEqualTo(pv.getId().toString());
        assertThat(buildConfiguration.getVersion()).isEqualTo(pv.getVersion());
    }

    @Test
    public void testGetAll() {
        Page<org.jboss.pnc.dto.ProductVersion> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(3)
                .haveExactly(
                        1,
                        new Condition<>(
                                version -> version.getVersion().equals(pv.getVersion()),
                                "ProductVersion present"));
    }

    @Test
    public void testStore() {
        final Integer prodId = 12;
        final String version = "17.0";
        final String abbreviation = "PNC";
        final ProductRef product = ProductRef.refBuilder().id(prodId.toString()).abbreviation(abbreviation).build();
        final Product productDB = prepareProduct(prodId, abbreviation);
        when(productRepository.queryById(prodId)).thenReturn(productDB);
        when(systemConfig.getBrewTagPattern()).thenReturn("${product_short_name}-${product_version}-HI");
        org.jboss.pnc.dto.ProductVersion productVersion = org.jboss.pnc.dto.ProductVersion.builder()
                .productMilestones(Collections.emptyMap())
                .product(product)
                .version(version)
                .build();

        org.jboss.pnc.dto.ProductVersion stored = provider.store(productVersion);

        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(stored.getVersion()).isEqualTo(version);
        assertThat(stored.getProduct().getId()).isEqualTo(prodId.toString());
        assertThat(stored.getAttributes()).isNotNull();
        assertThat(stored.getAttributes().get(Attributes.BREW_TAG_PREFIX)).isNotNull();
    }

    @Test
    public void testStoreDuplicateShouldThrowException() {

        Set<ProductVersion> productVersionSet = new HashSet<>();
        productVersionSet.add(pv);

        final Product productDB = prepareProduct(pv.getProduct().getId(), pv.getProduct().getAbbreviation());
        productDB.setProductVersions(productVersionSet);
        when(productRepository.queryById(pv.getProduct().getId())).thenReturn(productDB);

        org.jboss.pnc.dto.ProductVersion duplicate = org.jboss.pnc.dto.ProductVersion.builder()
                .version(pv.getVersion())
                .product(ProductRef.refBuilder().id(pv.getProduct().getId().toString()).build())
                .productMilestones(Collections.emptyMap())
                .build();

        assertThatThrownBy(() -> provider.store(duplicate)).isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testUpdate() {
        final String newVersion = "19.0";

        org.jboss.pnc.dto.ProductVersion productVersion = org.jboss.pnc.dto.ProductVersion.builder()
                .id(pv.getId().toString())
                .version(newVersion)
                .product(ProductRef.refBuilder().id(pv.getProduct().getId().toString()).build())
                .productMilestones(Collections.emptyMap())
                .build();

        assertThat(productVersion.getVersion()).isNotEqualTo(pv.getVersion());

        provider.update(productVersion.getId(), productVersion);

        org.jboss.pnc.dto.ProductVersion updated = provider.getSpecific(productVersion.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getVersion()).isEqualTo(newVersion);
    }

    @Test
    public void testShouldThrowWhenUpdatingWithClosedMilestone() {
        String newVersion = "19.0";
        org.jboss.pnc.dto.ProductVersion withClosedMilestone = provider.getSpecific("2");
        org.jboss.pnc.dto.ProductVersion updated = withClosedMilestone.toBuilder().version(newVersion).build();

        assertThatThrownBy(() -> provider.update(withClosedMilestone.getId(), updated))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testShouldThrowWhenCurrentMilestoneIsClosed() {
        String newVersion = "19.0";
        org.jboss.pnc.dto.ProductVersion withClosedMilestone = provider.getSpecific("2");
        ProductMilestoneRef closedMilestone = withClosedMilestone.getProductMilestones().values().iterator().next();
        org.jboss.pnc.dto.ProductVersion updated = withClosedMilestone.toBuilder()
                .currentProductMilestone(closedMilestone)
                .build();

        ProductMilestone closedReturn = new ProductMilestone();
        closedReturn.setId(Integer.parseInt(closedMilestone.getId()));
        closedReturn.setEndDate(new Date());

        when(milestoneRepository.queryById(anyInt())).thenReturn(closedReturn);

        assertThatThrownBy(() -> provider.update(withClosedMilestone.getId(), updated))
                .isInstanceOf(InvalidEntityException.class);
    }

    private ProductVersion prepareProductVersion(String version, int prodId, ProductMilestone... milestones) {
        return ProductVersion.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .version(version)
                .product(prepareProduct(prodId, null))
                .productMilestones(new HashSet<>(Arrays.asList(milestones)))
                .build();
    }

    private Product prepareProduct(int id, String abbreviation) {
        return Product.Builder.newBuilder().id(id).abbreviation(abbreviation).build();
    }

    private ProductMilestone prepareProductMilestone(int id, Date endDate) {
        return ProductMilestone.Builder.newBuilder().id(id).endDate(endDate).build();
    }
}
