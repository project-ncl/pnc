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

import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductMilestoneProviderTest extends AbstractIntIdProviderTest<ProductMilestone> {

    @Mock
    private ProductMilestoneRepository repository;

    @Mock
    private ProductMilestoneReleaseManager releaseManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProductMilestoneProviderImpl provider;

    private ProductMilestone mock = ProductMilestoneFactory.getInstance().prepareNewProductMilestone("1.2", "1.2.3.GA");
    private ProductMilestone mockSecond = ProductMilestoneFactory.getInstance()
            .prepareNewProductMilestone("1.1", "1.1.1.GA");

    @Before
    public void setup() {
        when(userService.currentUserToken()).thenReturn("eyUserToken");

        ProductMilestoneFactory.getInstance().setIdSupplier(() -> entityId.getAndIncrement());

        List<ProductMilestone> productMilestones = new ArrayList<>();

        mock.setPerformedBuilds(new HashSet<BuildRecord>(Arrays.asList(new BuildRecord())));

        productMilestones.add(mock);
        productMilestones.add(mockSecond);
        productMilestones.add(ProductMilestoneFactory.getInstance().prepareNewProductMilestone("1.3", "1.3.2.GA"));
        productMilestones.add(ProductMilestoneFactory.getInstance().prepareNewProductMilestone("5.5", "5.5.5.CR1"));

        fillRepository(productMilestones);
    }

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<ProductMilestone, Integer> repository() {
        return repository;
    }

    @Test
    public void testStoreNewProductMilestoneWithoutId() {

        // when
        org.jboss.pnc.dto.ProductMilestone toCreate = createNewProductMilestoneDTO(
                mock.getProductVersion(),
                "6.6.6.CR1");
        org.jboss.pnc.dto.ProductMilestone created = provider.store(toCreate);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getVersion()).isEqualTo(toCreate.getVersion());
        assertThat(created.getProductVersion().getId()).isEqualTo(toCreate.getProductVersion().getId());
    }

    @Test
    public void testStoreNewProductMilestoneWithIdShouldFail() {

        // when
        org.jboss.pnc.dto.ProductMilestone toCreate = createNewProductMilestoneDTO(
                mock.getProductVersion(),
                "6.6.7.CR1",
                Integer.toString(entityId.getAndIncrement()));

        // then
        assertThatThrownBy(() -> provider.store(toCreate)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreSameProductVersionShouldFail() {

        // when
        org.jboss.pnc.dto.ProductMilestone toCreate = createNewProductMilestoneDTO(
                mock.getProductVersion(),
                mock.getVersion());

        when(repository.queryByPredicates(any(Predicate.class))).thenReturn(mock);

        // then
        assertThatThrownBy(() -> provider.store(toCreate)).isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testStoreBadVersionsShouldFail() {

        assertThatThrownBy(() -> provider.store(createNewProductMilestoneDTO(mock.getProductVersion(), "1.2")))
                .isInstanceOf(InvalidEntityException.class);

        assertThatThrownBy(() -> provider.store(createNewProductMilestoneDTO(mock.getProductVersion(), "a.b.c.d")))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testUpdate() {
        // when
        String newVersion = "666" + mock.getVersion();

        org.jboss.pnc.dto.ProductMilestone toUpdate = org.jboss.pnc.dto.ProductMilestone.builder()
                .id(mock.getId().toString())
                .productVersion(productVersionMapper.toRef(mock.getProductVersion()))
                .version(newVersion)
                .build();

        provider.update(toUpdate.getId(), toUpdate);

        org.jboss.pnc.dto.ProductMilestone updated = provider.getSpecific(toUpdate.getId());

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(toUpdate.getId());
        assertThat(updated.getProductVersion().getId()).isEqualTo(toUpdate.getProductVersion().getId());
        assertThat(updated.getVersion()).isEqualTo(newVersion);
    }

    @Test
    public void testUpdateToExistingProductVersionShouldFail() {

        // when
        org.jboss.pnc.dto.ProductMilestone toUpdate = org.jboss.pnc.dto.ProductMilestone.builder()
                .id(mockSecond.getId().toString())
                .productVersion(productVersionMapper.toRef(mock.getProductVersion()))
                .version(mock.getVersion())
                .build();

        when(repository.queryByPredicates(any(Predicate.class))).thenReturn(mock);

        // then
        assertThatThrownBy(() -> provider.update(toUpdate.getId(), toUpdate))
                .isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testGetAll() {
        // when
        Page<org.jboss.pnc.dto.ProductMilestone> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(4);
    }

    @Test
    public void testGetSpecific() {

        // when
        org.jboss.pnc.dto.ProductMilestone milestone = provider.getSpecific(mock.getId().toString());

        // then
        assertThat(milestone).isNotNull();
        assertThat(milestone.getId()).isEqualTo(mock.getId().toString());
        assertThat(milestone.getVersion()).isEqualTo(mock.getVersion());
        assertThat(milestone.getProductVersion().getId()).isEqualTo(mock.getProductVersion().getId().toString());
    }

    @Test
    public void testGetProductMilestonesForProductVersion() {

        // when
        ProductVersion pv = mock.getProductVersion();

        Page<org.jboss.pnc.dto.ProductMilestone> page = provider
                .getProductMilestonesForProductVersion(0, 10, null, null, pv.getId().toString());

        // then
        assertThat(page.getContent().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testCancelMilestoneCloseProcess() {

        // when
        when(releaseManager.noReleaseInProgress(any())).thenReturn(false);
        provider.cancelMilestoneCloseProcess(mock.getId().toString());

        // then
        verify(releaseManager, times(1)).cancel(any());
    }

    @Test
    public void testCancelMilestoneCloseProcessShouldFailIfAlreadyClosed() {
        // given
        ProductMilestone closed = ProductMilestoneFactory.getInstance()
                .createNewProductMilestoneFromProductVersion(mock.getProductVersion(), "9.8.7.GA");
        closed.setEndDate(new Date());
        repositoryList.add(closed);

        // when then
        assertThatThrownBy(() -> provider.cancelMilestoneCloseProcess(closed.getId().toString()))
                .isInstanceOf(RepositoryViolationException.class);
    }

    @Test
    public void testCancelMilestoneCloseProcessShouldFailIfNoReleaseInProgress() {

        // when
        when(releaseManager.noReleaseInProgress(any())).thenReturn(true);

        assertThatThrownBy(() -> provider.cancelMilestoneCloseProcess(mock.getId().toString()))
                .isInstanceOf(EmptyEntityException.class);
    }

    @Test
    public void testCloseMilestoneShouldFailIfAlreadyClosed() {
        // given
        ProductMilestone closed = ProductMilestoneFactory.getInstance()
                .createNewProductMilestoneFromProductVersion(mock.getProductVersion(), "9.8.7.GA");
        closed.setEndDate(new Date());
        repositoryList.add(closed);

        org.jboss.pnc.dto.ProductMilestone milestone = provider.getSpecific(closed.getId().toString());

        // when then
        assertThatThrownBy(() -> provider.closeMilestone(milestone.getId()))
                .isInstanceOf(RepositoryViolationException.class);
    }

    @Test
    public void testCloseMilestone() {

        // when
        when(releaseManager.noReleaseInProgress(any())).thenReturn(true);
        provider.closeMilestone(mock.getId().toString());

        // then
        verify(releaseManager, times(1)).startRelease(any(), any(), eq(true), any());
    }

    private org.jboss.pnc.dto.ProductMilestone createNewProductMilestoneDTO(
            ProductVersion pv,
            String milestoneVersion,
            String id) {
        return org.jboss.pnc.dto.ProductMilestone.builder()
                .version(milestoneVersion)
                .productVersion(productVersionMapper.toRef(pv))
                .id(id)
                .build();
    }

    private org.jboss.pnc.dto.ProductMilestone createNewProductMilestoneDTO(
            ProductVersion pv,
            String milestoneVersion) {
        return createNewProductMilestoneDTO(pv, milestoneVersion, null);
    }
}
