/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.dingroguclient.DingroguClient;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.facade.util.ReqourClient;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private ReqourClient reqourClient;

    @Mock
    private GlobalModuleGroup globalConfig;

    @Mock
    private DingroguClient dingroguClient;

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
    public void testStoreNewRepositoryWithGithubInternalUrlInScpFormatShouldSucceed() {

        // when
        SCMRepository toCreate = createNewSCMRepository(
                "https://" + UUID.randomUUID().toString() + ".ca",
                "git@github.company.com:haha/hoho.git",
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
        when(reqourClient.translateExternalUrl(external)).thenReturn(mockInternalOnly.getInternalUrl());

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
        when(reqourClient.translateExternalUrl(external)).thenReturn(mock.getInternalUrl());

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
    public void testCreateNewRepositoryWithGitlabInternalUrlInScpFormatShouldFail() throws ConfigurationParseException {
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);
        when(scmModuleConfig.getBannedScmAuthority()).thenReturn("gitlab.company.com");

        assertThatThrownBy(() -> provider.createSCMRepository("git@gitlab.company.com:haha/hoho.git", true))
                .isInstanceOf(InvalidEntityException.class);
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
    public void testCreateSCMRepositoryWithSecondaryInternalRepository() {

        // when
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("internalrepo.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn("random.com");
        String internalUrl = "git+ssh://random.com/project-ncl/cleaner.git";

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

    private static final String SSLIP_URL = "http://169.63.101.183.sslip.io/lightwell-builds/github.com-springfox-springfox.git";

    @Test
    public void testIsInternalRepositoryRejectsLookAlikeHosts() {
        String authority = "github.ibm.com";
        // the reported prod sample
        assertFalse(SCMRepositoryProviderImpl.isInternalRepository(authority, null, SSLIP_URL));
        // authority in the path
        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository(authority, null, "git+ssh://x.net/github.ibm.com/y.git"));
        assertFalse(SCMRepositoryProviderImpl.isInternalRepository(authority, null, "x/github.ibm.com/y.git"));
        // authority as prefix / suffix of another host
        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository(authority, null, "git@github.ibm.com.evil.net:pnc-prod/x.git"));
        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository(authority, null, "git+ssh://github.ibm.com.evil.net/pnc-prod/x.git"));
        assertFalse(
                SCMRepositoryProviderImpl
                        .isInternalRepository(authority, null, "git@evil-github.ibm.com:pnc-prod/x.git"));
    }

    @Test
    public void testIsInternalRepositoryAcceptsConfiguredHosts() {
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("github.ibm.com", null, "git@github.ibm.com:pnc-prod/x.git"));
        assertTrue(
                SCMRepositoryProviderImpl.isInternalRepository(
                        "github.ibm.com",
                        "git.secondary.com",
                        "git@git.secondary.com:pnc-prod/x.git"));
        // authority with a port
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("github.com:22", null, "git+ssh://github.com:22/org/x.git"));
    }

    @Test
    public void testScpGithubIbmUrlPassesInternalValidation() {
        configureInternalAuthority();
        provider.validateInternalRepository("git@github.ibm.com:pnc-prod/x.git"); // must not throw
    }

    private RepositoryCreationResult.Builder successResult(String internalUrl) {
        return RepositoryCreationResult.builder()
                .status(ResultStatus.SUCCESS)
                .repoCreatedSuccessfully(true)
                .internalScmUrl(internalUrl)
                .externalUrl("https://github.com/springfox/springfox.git")
                .preBuildSyncEnabled(true)
                .taskId(1L)
                .jobType(JobNotificationType.SCM_REPOSITORY_CREATION);
    }

    private void configureInternalAuthority() {
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("github.ibm.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);
    }

    @Test
    public void testRepositoryCreationCompletedSavesValidInternalUrl() {
        configureInternalAuthority();

        provider.repositoryCreationCompleted(successResult("git@github.ibm.com:pnc-prod/x.git").build());

        verify(repository).save(any());
        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(notifier).sendMessage(sent.capture());
        assertThat(sent.getValue()).isInstanceOf(SCMRepositoryCreationSuccess.class);
    }

    @Test
    public void testRepositoryCreationCompletedRejectsSslipInternalUrl() {
        configureInternalAuthority();

        provider.repositoryCreationCompleted(successResult(SSLIP_URL).build()); // must not throw

        verify(repository, never()).save(any());
        assertCreationErrorSent();
    }

    @Test
    public void testRepositoryCreationCompletedRejectsNullInternalUrl() {
        provider.repositoryCreationCompleted(successResult(null).build()); // must not throw

        verify(repository, never()).save(any());
        assertCreationErrorSent();
    }

    /**
     * The internal authority appearing only in the path of a foreign host must not be routed to the internal branch:
     * the
     * URL is external, so a clone task is submitted and nothing is saved.
     */
    @Test
    public void testCreateSCMRepositoryWithAuthorityOnlyInPathIsExternal() {
        configureInternalAuthority();

        RepositoryCreationResponse response = provider
                .createSCMRepository("https://evil.net/github.ibm.com/x.git", true);

        assertThat(response.getTaskId()).isNotNull();
        assertThat(response.getRepository()).isNull();
        verify(dingroguClient).submitRepositoryCreation(any());
        verify(repository, never()).save(any());
    }

    /** An scp-like URL on the internal authority must be routed to the internal branch, not cloned as external. */
    @Test
    public void testCreateSCMRepositoryWithScpUrlOnInternalAuthorityIsInternal() {
        configureInternalAuthority();

        RepositoryCreationResponse response = provider
                .createSCMRepository("git@github.ibm.com:pnc-prod/x.git", true);

        assertThat(response.getTaskId()).isNull();
        assertThat(response.getRepository()).isNotNull();
        verify(repository).save(any());
        verify(dingroguClient, never()).submitRepositoryCreation(any());
    }

    /** The host is compared case-insensitively, for both routing and validation. */
    @Test
    public void testCreateSCMRepositoryHostMatchIsCaseInsensitive() {
        configureInternalAuthority();

        RepositoryCreationResponse response = provider
                .createSCMRepository("git@GITHUB.IBM.COM:pnc-prod/x.git", true);

        assertThat(response.getTaskId()).isNull();
        assertThat(response.getRepository()).isNotNull();
        verify(dingroguClient, never()).submitRepositoryCreation(any());
    }

    /**
     * Github internal URLs must be in scp format (NCL-8685). A non-scp URL on the internal authority, with or without
     * user info, is routed to the internal branch and rejected there, not cloned as external.
     */
    @Test
    public void testCreateSCMRepositoryGithubNonScpUrlIsRoutedInternalAndRejected() {
        configureInternalAuthority();

        for (String url : new String[] {
                "git+ssh://github.ibm.com/pnc-prod/x.git",
                "git+ssh://git@github.ibm.com/pnc-prod/x.git" }) {
            assertThatThrownBy(() -> provider.createSCMRepository(url, true)).as(url)
                    .isInstanceOf(InvalidEntityException.class)
                    .hasMessageContaining("git@<server>:<path to git repo>.git");
        }
        verify(dingroguClient, never()).submitRepositoryCreation(any());
        verify(repository, never()).save(any());
    }

    /** User info is ignored when matching the host; other providers may use 'git+ssh://git@host/...' (NCL-8685). */
    @Test
    public void testCreateSCMRepositoryNonGithubUrlWithUserInfoIsInternal() {
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("gitlab.company.com");
        when(scmModuleConfig.getSecondaryInternalScmAuthority()).thenReturn(null);

        RepositoryCreationResponse response = provider
                .createSCMRepository("git+ssh://git@gitlab.company.com/group/x.git", true);

        assertThat(response.getTaskId()).isNull();
        assertThat(response.getRepository()).isNotNull();
        verify(dingroguClient, never()).submitRepositoryCreation(any());
    }

    @Test
    public void testIsInternalRepositoryIsCaseInsensitiveAndIgnoresUrlPortForAuthorityWithPort() {
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("github.ibm.com", null, "git@GITHUB.IBM.COM:pnc-prod/x.git"));
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("GitHub.IBM.com", null, "git@github.ibm.com:pnc-prod/x.git"));
        // an authority configured with a port matches the host whatever port the URL carries
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("github.com:22", null, "git+ssh://github.com/org/x.git"));
        assertTrue(
                SCMRepositoryProviderImpl
                        .isInternalRepository("github.com:22", null, "git+ssh://github.com:2222/org/x.git"));
    }

    /** A '#ref' fragment is validated against the repository name pattern for scp-like and scheme URLs alike. */
    @Test
    public void testIsInternalRepositoryValidatesTheFragment() {
        for (String url : new String[] {
                "git@github.ibm.com:pnc-prod/x.git#abc123",
                "git@github.ibm.com:pnc-prod/x.git#v1.0-rc_1",
                "git+ssh://github.ibm.com/pnc-prod/x.git#abc123" }) {
            assertTrue(url, SCMRepositoryProviderImpl.isInternalRepository("github.ibm.com", null, url));
        }
        for (String url : new String[] {
                "git@github.ibm.com:pnc-prod/x.git#bad ref",
                "git@github.ibm.com:pnc-prod/x.git#a!b",
                "git@github.ibm.com:pnc-prod/x.git#",
                "git+ssh://github.ibm.com/pnc-prod/x.git#bad ref",
                "git+ssh://github.ibm.com/pnc-prod/x.git#a!b",
                "git+ssh://github.ibm.com/pnc-prod/x.git#" }) {
            assertFalse(url, SCMRepositoryProviderImpl.isInternalRepository("github.ibm.com", null, url));
        }
    }

    /** A URL that cannot be parsed is not on the internal host; it must not fail with an exception (a 500). */
    @Test
    public void testIsInternalRepositoryIsFalseForUnparsableUrl() {
        for (String url : new String[] {
                "git@evil.net#github.ibm.com:pnc-prod/x.git",
                "x",
                "git@:a",
                "https://",
                "not a url" }) {
            assertFalse(url, SCMRepositoryProviderImpl.isInternalRepository("github.ibm.com", null, url));
        }
    }

    /** An unparsable URL is routed as external (cloned), not failed with an exception (a 500). */
    @Test
    public void testCreateSCMRepositoryWithUnparsableUrlIsRoutedExternal() {
        configureInternalAuthority();

        RepositoryCreationResponse response = provider.createSCMRepository("git@evil.net#github.ibm.com:x.git", true);

        assertThat(response.getTaskId()).isNotNull();
        verify(dingroguClient).submitRepositoryCreation(any());
        verify(repository, never()).save(any());
    }

    /** An scp URL with a (valid) fragment on the internal authority is saved, not cloned. */
    @Test
    public void testCreateSCMRepositoryWithScpUrlAndFragmentIsInternal() {
        configureInternalAuthority();

        RepositoryCreationResponse response = provider
                .createSCMRepository("git@github.ibm.com:pnc-prod/x.git#abc123", true);

        assertThat(response.getTaskId()).isNull();
        assertThat(response.getRepository()).isNotNull();
        verify(repository).save(any());
        verify(dingroguClient, never()).submitRepositoryCreation(any());
    }

    private void assertCreationErrorSent() {
        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(notifier).sendMessage(sent.capture());
        assertThat(sent.getValue()).isInstanceOf(RepositoryCreationFailure.class);
        RepositoryCreationFailure failure = (RepositoryCreationFailure) sent.getValue();
        assertThat(failure.getNotificationType()).isEqualTo("RC_REPO_CREATION_ERROR");
        assertThat(failure.getTaskId()).isEqualTo("1");
        assertThat(failure.getMessage()).isNotEmpty();
    }

}
