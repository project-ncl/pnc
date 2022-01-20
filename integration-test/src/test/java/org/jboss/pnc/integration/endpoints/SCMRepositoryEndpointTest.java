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
package org.jboss.pnc.integration.endpoints;

import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
@Ignore // TODO enable me
public class SCMRepositoryEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(SCMRepositoryEndpointTest.class);

    SCMRepositoryClient repositoryClient = new SCMRepositoryClient(RestClientConfiguration.asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void testGetBuildConfigurationForARepository() throws ClientException {
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(
                RestClientConfiguration.asUser());
        SCMRepository scmRepository = repositoryClient.getAll(null, null).iterator().next();

        Iterator<BuildConfiguration> allConfigsIterator = buildConfigurationClient.getAll().iterator();

        BuildConfiguration buildConfiguration1 = allConfigsIterator.next()
                .toBuilder()
                .scmRepository(scmRepository)
                .build();
        BuildConfiguration buildConfiguration2 = allConfigsIterator.next()
                .toBuilder()
                .scmRepository(scmRepository)
                .build();

        buildConfigurationClient.update(buildConfiguration1.getId(), buildConfiguration1);
        buildConfigurationClient.update(buildConfiguration2.getId(), buildConfiguration2);

        RemoteCollection<BuildConfiguration> buildConfigs = repositoryClient.getBuildConfigs(scmRepository.getId());

        assertThat(buildConfigs)
                .usingElementComparatorIgnoringFields("modificationTime", "creationUser", "modificationUser")
                .contains(buildConfiguration1, buildConfiguration2)
                .allSatisfy((bc -> scmRepository.equals(bc.getScmRepository())));
    }

    @Test
    public void shouldCreateNewWithInternalUrl() throws ClientException {
        // With
        SCMRepository repository = SCMRepository.builder()
                .internalUrl("ssh://git@github.com:22/newUser/newRepo.git")
                .preBuildSyncEnabled(false)
                .build();

        CreateAndSyncSCMRequest request = CreateAndSyncSCMRequest.builder()
                .scmUrl(repository.getInternalUrl())
                .preBuildSyncEnabled(repository.getPreBuildSyncEnabled())
                .build();
        // When
        RepositoryCreationResponse response = repositoryClient.createNew(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRepository().getId()).isNotNull();

        SCMRepository refreshed = repositoryClient.getSpecific(response.getRepository().getId());
        assertThat(refreshed).isEqualToIgnoringGivenFields(repository, "id");
    }

    @Test
    public void shouldFailOnCreatingNewWithConflictingInternalUrl() {
        SCMRepository repository = SCMRepository.builder()
                .internalUrl("ssh://git@github.com:22/project-ncl/pnc.git") // from DatabaseDataInitializer.class
                .preBuildSyncEnabled(false)
                .build();

        CreateAndSyncSCMRequest request = CreateAndSyncSCMRequest.builder()
                .scmUrl(repository.getInternalUrl())
                .preBuildSyncEnabled(repository.getPreBuildSyncEnabled())
                .build();

        assertThatThrownBy(() -> repositoryClient.createNew(request)).hasCauseInstanceOf(ClientErrorException.class)
                .has(
                        new Condition<Throwable>(
                                (e -> ((ClientErrorException) e.getCause()).getResponse().getStatus() == 409),
                                "Has Cause with conflicted status code 409"));

    }

    @Test
    public void shouldFailToCreateNewWithInvalidUrl() {
        SCMRepository repository = SCMRepository.builder()
                .internalUrl("I will totally fail!!") // from DatabaseDataInitializer
                .preBuildSyncEnabled(false)
                .build();

        CreateAndSyncSCMRequest request = CreateAndSyncSCMRequest.builder()
                .scmUrl(repository.getInternalUrl())
                .preBuildSyncEnabled(repository.getPreBuildSyncEnabled())
                .build();

        assertThatThrownBy(() -> repositoryClient.createNew(request)).hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldUpdate() throws ClientException {
        // with
        SCMRepository scmRepository = repositoryClient.getAll(null, null).iterator().next();
        SCMRepository toUpdate = scmRepository.toBuilder()
                .externalUrl("http://newUrl.com/newproject/project.git")
                .build();

        // when
        repositoryClient.update(scmRepository.getId(), toUpdate);

        // then
        SCMRepository refreshed = repositoryClient.getSpecific(toUpdate.getId());
        assertThat(refreshed).isEqualToComparingFieldByField(toUpdate);
    }

    @Test
    public void shouldNotAllowUpdatingInternalUrl() throws ClientException {
        // then
        final String validInternalUrl = "http://git@github.com:22/project-ncl/pnc22.git";
        SCMRepository scmRepository = repositoryClient.getAll(null, null).iterator().next();
        SCMRepository toUpdate = scmRepository.toBuilder().internalUrl(validInternalUrl).build();

        // when/then
        assertThatThrownBy(() -> repositoryClient.update(toUpdate.getId(), toUpdate))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldGetConflictWhenUpdatingExternalUrlToExistingOne() throws ClientException {
        Iterator<SCMRepository> iterator = repositoryClient.getAll(null, null).iterator();
        String existingExternalUrl = iterator.next().getExternalUrl();
        SCMRepository randomScmRepository = iterator.next();

        SCMRepository toUpdate = randomScmRepository.toBuilder().externalUrl(existingExternalUrl).build();

        assertThatThrownBy(() -> repositoryClient.update(toUpdate.getId(), toUpdate))
                .hasCauseInstanceOf(ClientErrorException.class)
                .has(
                        new Condition<Throwable>(
                                (e -> ((ClientErrorException) e.getCause()).getResponse().getStatus() == 409),
                                "HTTP 409 Conflict"));
    }

    @Test
    public void shouldMatchFullExternalRepositoryUrl() throws ClientException {
        SCMRepository repository = repositoryClient.getSpecific("100");
        final String requestUrl1 = "ssh://github.com:22/project-ncl/pnc";
        final String requestUrl2 = "ssh://github.com:22/project-ncl/pnc.git";
        final String requestUrl3 = "github.com/project-ncl/pnc";

        assertThat(repositoryClient.getAll(requestUrl1, null)).containsExactly(repository);
        assertThat(repositoryClient.getAll(requestUrl2, null)).containsExactly(repository);
        assertThat(repositoryClient.getAll(requestUrl3, null)).containsExactly(repository);
    }

    @Test
    public void shouldNotMatchPartialExternalRepositoryUrl() throws ClientException {
        // similar to RC:100 in DatabaseDataInitializer
        final String requestUrl1 = "https://github.com";
        final String requestUrl2 = "ssh://github.com/project-ncl";

        assertThat(repositoryClient.getAll(requestUrl1, null)).isEmpty();
        assertThat(repositoryClient.getAll(requestUrl2, null)).isEmpty();
    }

    @Test
    public void testGetBuildConfigs() throws RemoteResourceException {
        // when
        RemoteCollection<BuildConfiguration> bcs = repositoryClient.getBuildConfigs("100");

        // then
        assertThat(bcs).hasSize(1)
                .are(new Condition<>(buildConfiguration -> buildConfiguration.getId().equals("1"), "Is BC with id 1"));
    }

    @Test
    public void testGetBuildConfigsWithInvalidId() {
        // when/then
        assertThatThrownBy(() -> repositoryClient.getBuildConfigs("45645644"))
                .hasCauseInstanceOf(BadRequestException.class);
    }
}
