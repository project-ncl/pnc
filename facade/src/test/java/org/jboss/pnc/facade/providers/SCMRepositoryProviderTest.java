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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.pnc.facade.util.RepourClient;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SCMRepositoryProviderTest extends AbstractIntIdProviderTest<RepositoryConfiguration> {

    @Mock
    private RepositoryConfigurationRepository repository;

    @Mock
    private Notifier notifier;

    @Mock
    private Configuration configuration;

    @Mock
    private ScmModuleConfig scmModuleConfig;

    @Mock
    private RepourClient repour;

    @InjectMocks
    private SCMRepositoryProviderImpl provider;

    private RepositoryConfiguration mock = createNewRepositoryConfiguration(
            "http://external.sh",
            "git+ssh://internal.sh",
            true);

    private RepositoryConfiguration mockSecond = createNewRepositoryConfiguration(
            "http://external2.sh",
            "git+ssh://internale.sh",
            false);

    private RepositoryConfiguration mockInternalOnly = createNewRepositoryConfiguration(
            null,
            "git+ssh://internal.sh/foo/bar",
            true);

    @Before
    public void setup() {

        List<RepositoryConfiguration> list = new ArrayList<>();

        list.add(mock);
        list.add(mockSecond);
        list.add(mockInternalOnly);

        list.add(
                createNewRepositoryConfiguration(
                        "https://" + UUID.randomUUID().toString() + ".ca",
                        "git+ssh://" + UUID.randomUUID().toString() + ".eu",
                        true));

        list.forEach(this::setupRC);

        fillRepository(list);
    }

    private void setupRC(RepositoryConfiguration rc) {
        when(repository.queryByInternalScm(rc.getInternalUrl())).thenReturn(rc);
        if (rc.getExternalUrl() != null) {
            when(repository.queryByExternalScm(rc.getExternalUrl())).thenReturn(rc);
        }
    }

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<RepositoryConfiguration, Integer> repository() {
        return repository;
    }

    @Test
    public void testStoreNewRepositoryWithoutId() {
        // when
        SCMRepository toCreate = createNewSCMRepository(
                "https://" + UUID.randomUUID().toString() + ".ca",
                "git+ssh://" + UUID.randomUUID().toString() + ".eu",
                true,
                null);

        SCMRepository created = provider.store(toCreate);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull().isNotEmpty();
        assertThat(created.getInternalUrl()).isEqualTo(toCreate.getInternalUrl());
        assertThat(created.getExternalUrl()).isEqualTo(toCreate.getExternalUrl());
        assertThat(created.getPreBuildSyncEnabled()).isEqualTo(toCreate.getPreBuildSyncEnabled());
    }

    @Test
    public void testStoreNewRepositoryWithIdShouldFail() {

        // when
        SCMRepository toCreate = createNewSCMRepository(
                "https://" + UUID.randomUUID().toString() + ".ca",
                "git+ssh://" + UUID.randomUUID().toString() + ".eu",
                true,
                Integer.toString(entityId.getAndIncrement()));

        // then
        assertThatThrownBy(() -> provider.store(toCreate)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreNewRepositoryWithInvalidInternalUrlShouldFail() {

        // when
        SCMRepository toCreate = createNewSCMRepository(
                "https://" + UUID.randomUUID().toString() + ".ca",
                "noway" + UUID.randomUUID().toString(),
                true,
                null);

        // then
        assertThatThrownBy(() -> provider.store(toCreate)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreNewRepositoryWithGitlabInternalUrlInScpFormatShouldSucceed() {

        // when
        SCMRepository toCreate = createNewSCMRepository(
                "https://" + UUID.randomUUID().toString() + ".ca",
                "git@gitlab.com:haha/hoho.git",
                true,
                null);

        SCMRepository created = provider.store(toCreate);
        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull().isNotEmpty();
        assertThat(created.getInternalUrl()).isEqualTo(toCreate.getInternalUrl());
    }

    @Test
    public void testUpdate() {

        // when
        boolean newPreSync = !mock.isPreBuildSyncEnabled();
        SCMRepository toUpdate = createNewSCMRepository(
                mock.getExternalUrl(),
                mock.getInternalUrl(),
                newPreSync,
                mock.getId().toString());

        provider.update(toUpdate.getId(), toUpdate);
        SCMRepository updated = provider.getSpecific(toUpdate.getId());

        // then
        assertThat(updated.getId()).isEqualTo(mock.getId().toString());
        assertThat(updated.getExternalUrl()).isEqualTo(mock.getExternalUrl());
        assertThat(updated.getInternalUrl()).isEqualTo(mock.getInternalUrl());
        assertThat(updated.getPreBuildSyncEnabled()).isEqualTo(newPreSync);
    }

    @Test
    public void testUpdateWithUpdatedInternalUrlShouldFail() {

        // when
        SCMRepository toUpdate = createNewSCMRepository(
                mock.getExternalUrl(),
                mock.getInternalUrl() + "ssss",
                mock.isPreBuildSyncEnabled(),
                mock.getId().toString());

        // then
        assertThatThrownBy(() -> provider.update(toUpdate.getId(), toUpdate))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testUpdateWithOwnUrl() {
        // with
        String external = "http://external.sh/foo/bar";
        when(repour.translateExternalUrl(external)).thenReturn(mockInternalOnly.getInternalUrl());

        SCMRepository toUpdate = createNewSCMRepository(
                external,
                mockInternalOnly.getInternalUrl(),
                mockInternalOnly.isPreBuildSyncEnabled(),
                mockInternalOnly.getId().toString());

        // when
        SCMRepository updated = provider.update(toUpdate.getId(), toUpdate);

        // then
        assertThat(updated.getExternalUrl()).isEqualTo(external);
    }

    @Test
    public void testUpdateShouldFailWithConflict() {
        // with
        String external = "http://external.sh/foo/bar";
        when(repour.translateExternalUrl(external)).thenReturn(mock.getInternalUrl());

        SCMRepository toUpdate1 = createNewSCMRepository(
                mockSecond.getExternalUrl(),
                mockInternalOnly.getInternalUrl(),
                mockInternalOnly.isPreBuildSyncEnabled(),
                mockInternalOnly.getId().toString());

        SCMRepository toUpdate2 = createNewSCMRepository(
                external,
                mockInternalOnly.getInternalUrl(),
                mockInternalOnly.isPreBuildSyncEnabled(),
                mockInternalOnly.getId().toString());

        // when-then
        assertThatThrownBy(() -> provider.update(toUpdate1.getId(), toUpdate1))
                .isInstanceOf(ConflictedEntryException.class);
        assertThatThrownBy(() -> provider.update(toUpdate2.getId(), toUpdate2))
                .isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testGetAll() {

        // when
        Page<SCMRepository> page = provider.getAll(0, 10, null, null);

        // then
        assertThat(page.getContent()).hasSize(4);
    }

    @Test
    public void testGetSpecific() {

        // when
        SCMRepository repo = provider.getSpecific(mock.getId().toString());

        // then
        assertThat(repo).isNotNull();
        assertThat(repo.getId()).isEqualTo(mock.getId().toString());
        assertThat(repo.getExternalUrl()).isEqualTo(mock.getExternalUrl());
        assertThat(repo.getInternalUrl()).isEqualTo(mock.getInternalUrl());
        assertThat(repo.getPreBuildSyncEnabled()).isEqualTo(mock.isPreBuildSyncEnabled());
    }

    @Test
    public void testCreateSCMRepository() throws ConfigurationParseException {

        // when
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("github.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);

        RepositoryCreationResponse response = provider
                .createSCMRepository("git+ssh://github.com/project-ncl/cleaner.git", true);

        // then
        // NCL-5989 don't send WS message if repository is created immediately (happens with internal url)
        // verify(notifier, times(1)).sendMessage(any());
        assertThat(response).isNotNull();

        if (response.getRepository() == null && response.getTaskId() == null) {
            Assert.fail("Both repository and task id cannot be null!");
        }
    }

    @Test
    public void testCreateSCMRepositoryWithWrongUrlShouldFail() throws ConfigurationParseException {

        // when
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("github.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);

        // then
        assertThatThrownBy(() -> provider.createSCMRepository("git+ssh://github.com/notvalid", true))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testCreateSCMRepositoryWithWrongUrlProtocolShouldFail() throws ConfigurationParseException {

        // when
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("github.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);

        // then
        assertThatThrownBy(() -> provider.createSCMRepository("https://github.com/project-ncl/cleaner.git", true))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testCreateSCMRepositoryEmptyUrlShouldFail() {

        // when, then
        assertThatThrownBy(() -> provider.createSCMRepository("", true)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testCreateSCMRepositoryWithInvalidInternalRepositoryShouldFail() {

        // when
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("internalrepo.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);
        String invalidInternal = scmModuleConfig.getInternalScmAuthority() + "/gerrit/random-project.git";

        // then
        assertThatThrownBy(() -> provider.createSCMRepository(invalidInternal, false))
                .isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testCreateSCMRepositoryWithSecondaryInternalRepository() {

        // when
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("internalrepo.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn("github.com");
        String internalUrl = "git+ssh://github.com/project-ncl/cleaner.git";

        RepositoryCreationResponse response = provider.createSCMRepository(internalUrl, false);

        // then
        assertThat(response).isNotNull();

        if (response.getRepository() == null) {
            Assert.fail("repository must not be null when creating SCM repository from internal SCM URL!");
        }
        if (response.getTaskId() != null) {
            Assert.fail("taskId must be null when creating SCM repository from internal SCM URL!");
        }
    }

    @Test
    public void testGetAllWithMatchAndSearchUrl() {

        // I can't really test the search predicates here since repository is mocked. But I can test the other code path

        // when
        Page<SCMRepository> pageMatch = provider
                .getAllWithMatchAndSearchUrl(0, 10, null, null, mock.getExternalUrl(), null);

        Page<SCMRepository> pageSearch = provider
                .getAllWithMatchAndSearchUrl(0, 10, null, null, null, mock.getInternalUrl());

        Page<SCMRepository> pageMatchAndSearch = provider
                .getAllWithMatchAndSearchUrl(0, 10, null, null, mockSecond.getInternalUrl(), mock.getExternalUrl());

        // then
        assertThat(pageMatch.getContent()).isNotNull();
        assertThat(pageSearch.getContent()).isNotNull();
        assertThat(pageMatchAndSearch.getContent()).isNotNull();
    }

    @Test
    public void testDeleteShouldFail() {
        assertThatThrownBy(() -> provider.delete(mock.getId().toString()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    public RepositoryConfiguration createNewRepositoryConfiguration(String external, String internal, boolean preSync) {

        return RepositoryConfiguration.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .externalUrl(external)
                .internalUrl(internal)
                .preBuildSyncEnabled(preSync)
                .build();
    }

    public SCMRepository createNewSCMRepository(String external, String internal, boolean presync, String id) {

        return SCMRepository.builder()
                .id(id)
                .externalUrl(external)
                .internalUrl(internal)
                .preBuildSyncEnabled(presync)
                .build();
    }

    /**
     * Tests correct evaluation of internal repository based various combinations of primary and secondary internal SCM
     * authority.
     */
    @Test
    public void testIsInternalRepository() {
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("git.example.com", null, "git+ssh://git.example.com/group/repo.git"));
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("git.example.com", null, "git@git.example.com:group/repo.git"));

        assertTrue(
                SCMRepositoryProviderImpl.isInternalRepository(
                        "git.primary.com",
                        "git.secondary.com",
                        "git+ssh://git.primary.com/group/repo.git"));
        assertTrue(
                SCMRepositoryProviderImpl.isInternalRepository(
                        "git.primary.com",
                        "git.secondary.com",
                        "git@git.primary.com:group/repo.git"));

        assertTrue(
                SCMRepositoryProviderImpl.isInternalRepository(
                        "git.primary.com",
                        "git.secondary.com",
                        "git+ssh://git.secondary.com/group/repo.git"));
        assertTrue(
                SCMRepositoryProviderImpl.isInternalRepository(
                        "git.primary.com",
                        "git.secondary.com",
                        "git@git.secondary.com:group/repo.git"));

        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository("git.example.com", null, "git+ssh://git.another.com/group.repo.git"));
        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository("git.example.com", null, "git@git.another.com:group/repo.git"));
    }

}
