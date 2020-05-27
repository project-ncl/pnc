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

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ProductReleaseProviderTest extends AbstractIntIdProviderTest<ProductRelease> {

    @Mock
    private ProductReleaseRepository repository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVersionRepository productVersionRepository;

    @Mock
    private ProductMilestoneRepository productMilestoneRepository;

    @InjectMocks
    private ProductReleaseProviderImpl provider;

    private ProductMilestone milestoneMock = prepareNewProductMilestone();
    private ProductMilestone milestoneMockSecond = prepareNewProductMilestone();
    private ProductRelease releaseMock = prepareNewProductRelease("1.2.3.GA", milestoneMock);
    private ProductRelease releaseMockSecond = prepareNewProductRelease("1.2.4.GA", milestoneMockSecond);

    @Before
    public void setup() {
        List<ProductRelease> releases = new ArrayList<>();

        releases.add(releaseMock);
        releases.add(releaseMockSecond);

        fillRepository(releases);
    }

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<ProductRelease, Integer> repository() {
        return repository;
    }

    @Test
    public void testStoreNewProductReleaseWithoutId() {

        // when
        org.jboss.pnc.dto.ProductRelease releaseDTO = createNewProductReleaseDTO("9.6.9.GA");
        org.jboss.pnc.dto.ProductRelease releaseDTOSaved = provider.store(releaseDTO);

        // then
        assertThat(releaseDTOSaved.getId()).isNotNull().isNotEmpty();
        assertThat(releaseDTOSaved.getVersion()).isEqualTo(releaseDTO.getVersion());
        assertThat(releaseDTOSaved.getProductMilestone().getId()).isEqualTo(releaseDTO.getProductMilestone().getId());
    }

    @Test
    public void testStoreNewProductReleaseWithIdShouldFail() {

        // when
        org.jboss.pnc.dto.ProductRelease releaseDTO = createNewProductReleaseDTO("9.9.9.GA", entityId++);

        // then
        assertThatThrownBy(() -> provider.store(releaseDTO)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testUpdate() {

        // when
        String newVersion = "66" + releaseMock.getVersion();

        org.jboss.pnc.dto.ProductRelease toUpdate = org.jboss.pnc.dto.ProductRelease.builder()
                .id(releaseMock.getId().toString())
                .version(newVersion)
                .productMilestone(productMilestoneMapper.toRef(releaseMock.getProductMilestone()))
                .productVersion(productVersionMapper.toRef(releaseMock.getProductMilestone().getProductVersion()))
                .build();

        provider.update(toUpdate.getId(), toUpdate);

        org.jboss.pnc.dto.ProductRelease updated = provider.getSpecific(toUpdate.getId());

        // then
        assertThat(updated.getId()).isEqualTo(toUpdate.getId());
        assertThat(updated.getVersion()).isEqualTo(newVersion);
        assertThat(updated.getProductMilestone().getId()).isEqualTo(toUpdate.getProductMilestone().getId());
    }

    @Test
    public void testGetAll() {

        // when
        Page<org.jboss.pnc.dto.ProductRelease> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(2);
    }

    @Test
    public void testGetSpecific() {

        assertThat(releaseMock.getId()).isNotNull();

        // when
        org.jboss.pnc.dto.ProductRelease release = provider.getSpecific(releaseMock.getId().toString());

        // then
        assertThat(release.getId()).isEqualTo(releaseMock.getId().toString());
        assertThat(release.getVersion()).isEqualTo(releaseMock.getVersion());
    }

    @Test
    public void testGetProductReleasesForProductVersion() {

        Page<org.jboss.pnc.dto.ProductRelease> page = provider.getProductReleasesForProductVersion(
                0,
                10,
                null,
                null,
                milestoneMock.getProductVersion().getId().toString());

        assertThat(page.getContent().size()).isGreaterThanOrEqualTo(1);
    }

    private ProductMilestone prepareNewProductMilestone() {

        Product product = Product.Builder.newBuilder().id(entityId++).name(Sequence.nextId().toString()).build();

        ProductVersion productVersion = ProductVersion.Builder.newBuilder()
                .id(entityId++)
                .version("1.2")
                .product(product)
                .build();

        return ProductMilestone.Builder.newBuilder()
                .id(entityId++)
                .productVersion(productVersion)
                .version("1.2.3.CR1")
                .build();
    }

    private ProductRelease prepareNewProductRelease(String version, ProductMilestone milestone) {

        ProductRelease release = ProductRelease.Builder.newBuilder()
                .id(entityId++)
                .version(version)
                .productMilestone(milestone)
                .build();

        milestone.setProductRelease(release);

        return release;
    }

    private org.jboss.pnc.dto.ProductRelease createNewProductReleaseDTO(String version, Integer id) {

        // when
        ProductMilestone milestone = prepareNewProductMilestone();

        when(productRepository.queryById(milestone.getProductVersion().getProduct().getId()))
                .thenReturn(milestone.getProductVersion().getProduct());

        when(productVersionRepository.queryById(milestone.getProductVersion().getId()))
                .thenReturn(milestone.getProductVersion());

        when(productMilestoneRepository.queryById(milestone.getId())).thenReturn(milestone);

        ProductMilestoneRef ref = productMilestoneMapper.toRef(milestone);
        ProductVersionRef productVersionRef = productVersionMapper.toRef(milestone.getProductVersion());

        org.jboss.pnc.dto.ProductRelease.Builder releaseDTO = org.jboss.pnc.dto.ProductRelease.builder()
                .version(version)
                .productVersion(productVersionRef)
                .productMilestone(ref);

        if (id != null) {
            return releaseDTO.id(id.toString()).build();
        } else {
            return releaseDTO.build();
        }
    }

    private org.jboss.pnc.dto.ProductRelease createNewProductReleaseDTO(String version) {
        return createNewProductReleaseDTO(version, null);
    }
}
