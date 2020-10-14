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
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.ApacheHttpClient43EngineWithRetry;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientBase;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.demo.data.DatabaseDataInitializer;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import org.jboss.pnc.integration.setup.Credentials;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.BASE_REST_PATH;
import static org.jboss.pnc.rest.configuration.Constants.MAX_PAGE_SIZE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpointTest.class);
    private static String buildId;
    private static String build2Id;
    private static String build3Id;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        Iterator<Build> it = bc.getAll(null, null).iterator();
        buildId = it.next().getId();
        build2Id = it.next().getId();
        build3Id = it.next().getId();
    }

    @Test
    public void shouldSortResults() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        String sort = "=asc=submitTime";

        List<Long> notSorted = bc.getAll(null, null)
                .getAll()
                .stream()
                .map(BuildRef::getSubmitTime)
                .map(Instant::getEpochSecond)
                .collect(Collectors.toList());

        List<Long> sorted = bc.getAll(null, null, Optional.of(sort), Optional.empty())
                .getAll()
                .stream()
                .map(BuildRef::getSubmitTime)
                .map(Instant::getEpochSecond)
                .collect(Collectors.toList());

        assertThat(notSorted).isNotEqualTo(sorted);
        assertThat(sorted).isSorted();

    }

    @Test
    public void shouldFilterResults() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        String rsql = "id==1";

        List<String> filtered = bc.getAll(null, null, Optional.empty(), Optional.of(rsql))
                .getAll()
                .stream()
                .map(BuildRef::getId)
                .collect(Collectors.toList());

        assertThat(filtered).containsExactly("1");
    }

    @Test
    public void shouldSupportPaging() throws Exception {
        int pageSize = 1;

        for (int pageIndex = 0; pageIndex <= 3; pageIndex++) {
            final io.restassured.response.Response response = given().redirects()
                    .follow(false)
                    .port(8080)
                    .when()
                    .get(String.format(BASE_REST_PATH + "/builds/?pageIndex=%d&pageSize=%d", pageIndex, pageSize));

            List<Build> builds = response.getBody().jsonPath().getList("content", Build.class);

            assertThat(builds).hasSize(1);
        }
    }

    @Test
    public void shouldBeAbleToReachAllBuildsWhenPaging() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());

        List<String> buildIds = bc.getAll(null, null).getAll().stream().map(Build::getId).collect(Collectors.toList());
        List<String> pagedBuildIds = new ArrayList<>();
        int pageSize = 1;

        for (int pageIndex = 0; pageIndex < buildIds.size(); pageIndex++) {
            final io.restassured.response.Response response = given().redirects()
                    .follow(false)
                    .port(8080)
                    .when()
                    .get(String.format(BASE_REST_PATH + "/builds/?pageIndex=%d&pageSize=%d", pageIndex, pageSize));

            List<Build> builds = response.getBody().jsonPath().getList("content", Build.class);
            pagedBuildIds.add(builds.get(0).getId());
        }

        assertThat(pagedBuildIds).containsExactlyInAnyOrderElementsOf(buildIds);
    }

    @Test
    public void shouldReturnCorrectPageCount() {
        int pageSize = 1;

        final io.restassured.response.Response response = given().redirects()
                .follow(false)
                .port(8080)
                .when()
                .get(String.format(BASE_REST_PATH + "/builds/?pageSize=%d", pageSize));

        int pageCount = response.getBody().jsonPath().get("totalPages");

        assertThat(pageCount).isEqualTo(4);
    }

    @Test
    public void shouldFilterByUserId() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());

        Build build = bc.getAll(null, null).getAll().iterator().next();
        String userId = build.getUser().getId();
        String rsql = "user.id==" + userId;

        List<String> userIds = bc.getAll(null, null, Optional.empty(), Optional.of(rsql))
                .getAll()
                .stream()
                .map(Build::getUser)
                .map(User::getId)
                .collect(Collectors.toList());

        assertThat(userIds).hasSize(2); // from DatabaseDataInitializer
        assertThat(userIds).containsOnly(userId);
    }

    @Test
    public void shouldFilterByUsername() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        String username = "demo-user";
        String rsql = "user.username==" + username;

        List<String> userNames = bc.getAll(null, null, Optional.empty(), Optional.of(rsql))
                .getAll()
                .stream()
                .map(Build::getUser)
                .map(User::getUsername)
                .collect(Collectors.toList());

        assertThat(userNames).hasSize(2); // from DatabaseDataInitializer
        assertThat(userNames).containsOnly(username);
    }

    @Test
    public void shouldFilterByBuildConfigurationName() throws Exception {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        String buildConfigName = DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
        BuildsFilterParameters filter = new BuildsFilterParameters();
        filter.setBuildConfigName(buildConfigName);

        List<String> buildConfigNames = client.getAll(filter, null)
                .getAll()
                .stream()
                .map(Build::getBuildConfigRevision)
                .map(BuildConfigurationRevisionRef::getName)
                .collect(Collectors.toList());

        assertThat(buildConfigNames).hasSize(2); // from DatabaseDataInitializer
        assertThat(buildConfigNames).containsOnly(buildConfigName);
    }

    @Test
    public void shouldFilterByBuildConfigurationNameLike() throws Exception {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        String buildConfigName = DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
        BuildsFilterParameters filter = new BuildsFilterParameters();
        filter.setBuildConfigName("*" + buildConfigName.substring(1, buildConfigName.length() - 2) + "*");

        List<String> buildConfigNames = client.getAll(filter, null)
                .getAll()
                .stream()
                .map(Build::getBuildConfigRevision)
                .map(BuildConfigurationRevisionRef::getName)
                .collect(Collectors.toList());

        assertThat(buildConfigNames).hasSize(2); // from DatabaseDataInitializer
        assertThat(buildConfigNames).containsOnly(buildConfigName);
    }

    @Test
    public void shouldFilterByNotExistingBuildConfigurationName() throws Exception {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        String buildConfigName = "SomeRandomName";
        BuildsFilterParameters filter = new BuildsFilterParameters();
        filter.setBuildConfigName(buildConfigName);

        List<String> buildConfigNames = client.getAll(filter, null)
                .getAll()
                .stream()
                .map(Build::getBuildConfigRevision)
                .map(BuildConfigurationRevisionRef::getName)
                .collect(Collectors.toList());

        assertThat(buildConfigNames).isEmpty();
    }

    @Test
    public void shouldFilterByBuildConfigurationNameAndUserId() throws Exception {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String buildConfigName = DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
        String username = "pnc-admin";
        String rsql = "user.username==" + username;

        BuildsFilterParameters filter = new BuildsFilterParameters();
        filter.setBuildConfigName(buildConfigName);

        List<Build> builds = new ArrayList<>(client.getAll(filter, null, Optional.empty(), Optional.of(rsql)).getAll());

        assertThat(builds).hasSize(2);
        assertThat(
                builds.stream()
                        .map(Build::getBuildConfigRevision)
                        .map(BuildConfigurationRevisionRef::getName)
                        .allMatch(name -> name.equals(buildConfigName)));
        assertThat(builds.stream().map(Build::getUser).map(User::getUsername).allMatch(name -> name.equals(username)));
    }

    @Test
    public void shouldFilterByBuildConfigurationNameAndInvalidUserId() throws Exception {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        String buildConfigName = DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
        String username = "random-user-name";
        String rsql = "user.username==" + username;

        BuildsFilterParameters filter = new BuildsFilterParameters();
        filter.setBuildConfigName(buildConfigName);

        List<Build> builds = new ArrayList<>(client.getAll(filter, null, Optional.empty(), Optional.of(rsql)).getAll());

        assertThat(builds).isEmpty();
    }

    @Test
    @InSequence(10)
    public void shouldGetBuilds() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Build> all = client.getAll(null, null);

        assertThat(all).hasSize(4);
    }

    @Test
    public void shouldFailGetAllWithLargePage() throws RemoteResourceException {
        final Configuration clientConfig = RestClientConfiguration.asAnonymous();

        Configuration largePageConfig = Configuration.builder()
                .basicAuth(clientConfig.getBasicAuth())
                .bearerToken(clientConfig.getBearerToken())
                .host(clientConfig.getHost())
                .pageSize(MAX_PAGE_SIZE + 1)
                .port(clientConfig.getPort())
                .protocol(clientConfig.getProtocol())
                .build();
        BuildClient client = new BuildClient(largePageConfig);

        assertThatThrownBy(() -> client.getAll(null, null)).hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    @InSequence(10)
    public void shouldGetSpecificBuild() throws ClientException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        Build dto = client.getSpecific(buildId);

        assertThat(dto.getId()).isEqualTo(buildId); // from DatabaseDataInitializer
        assertThat(dto.getStatus()).isEqualTo(BuildStatus.SUCCESS); // from DatabaseDataInitializer
        assertThat(dto.getBuildConfigRevision()).isNotNull();
        assertThat(dto.getBuildConfigRevision().getName()).isNotNull();
    }

    @Test
    @InSequence(20)
    public void shouldUpdateBuild() throws ClientException {
        // given
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());

        Build original = client.getSpecific(buildId);
        Build toUpdate = original.toBuilder().status(BuildStatus.SYSTEM_ERROR).build();
        assertThat(toUpdate.getStatus()).isNotEqualTo(original.getStatus());

        // when
        client.update(buildId, toUpdate);
        Build updated = client.getSpecific(buildId);

        assertThat(updated.getId()).isEqualTo(buildId);
        assertThat(updated).isEqualToIgnoringGivenFields(original, "status");
        assertThat(updated.getStatus()).isNotEqualTo(original.getStatus());
        assertThat(updated.getStatus()).isEqualTo(toUpdate.getStatus());
    }

    @Test
    public void shouldSetBuiltArtifacts() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());

        String buildRecordId = "1";
        RemoteCollection<Artifact> artifacts = client.getBuiltArtifacts(buildRecordId);
        Set<Integer> artifactIds = artifactIds(artifacts);
        assertThat(artifactIds).containsExactlyInAnyOrder(100, 101);

        client.setBuiltArtifacts(buildRecordId, Collections.singletonList("104"));
        RemoteCollection<Artifact> newBuiltArtifacts = client.getBuiltArtifacts(buildRecordId);
        Set<Integer> updatedArtifactIds = artifactIds(newBuiltArtifacts);
        assertThat(updatedArtifactIds).containsExactlyInAnyOrder(104);
    }

    @Test
    public void shouldSetDependentArtifacts() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());

        String buildRecordId = "1";
        RemoteCollection<Artifact> artifacts = client.getDependencyArtifacts(buildRecordId);
        Set<Integer> artifactIds = artifactIds(artifacts);
        assertThat(artifactIds).contains(104, 105);

        client.setDependentArtifacts(buildRecordId, Collections.singletonList("102"));
        RemoteCollection<Artifact> newDependencyArtifacts = client.getDependencyArtifacts(buildRecordId);
        Set<Integer> updatedArtifactIds = artifactIds(newDependencyArtifacts);
        assertThat(updatedArtifactIds).contains(102);
    }

    @Test
    public void shouldGetSCMArchiveLink() throws ClientException, ReflectiveOperationException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        // Disable redirects so we can test the actual response
        Field f = ClientBase.class.getDeclaredField("client");
        f.setAccessible(true);
        ResteasyClient reClient = (ResteasyClient) f.get(client);
        ApacheHttpClient43EngineWithRetry engine = (ApacheHttpClient43EngineWithRetry) reClient.httpEngine();
        engine.setFollowRedirects(false);

        Response internalScmArchiveLink = client.getInternalScmArchiveLink(buildId);
        assertThat(internalScmArchiveLink.getStatusInfo()).isEqualTo(Status.TEMPORARY_REDIRECT);
        assertThat(internalScmArchiveLink.getHeaderString("Location")).isNotEmpty();
    }

    @Test
    public void shouldGetAndRemoveAttribute() throws ClientException {
        // given
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        Build original = client.getSpecific(buildId);
        final String key = "TEST_ATTRIBUTE";
        final String value = "test value";
        assertThat(original.getAttributes()).doesNotContainKey(key);

        // when
        client.addAttribute(buildId, key, value);

        // then
        Build withAttribute = client.getSpecific(buildId);
        assertThat(withAttribute.getAttributes()).contains(entry(key, value));

        // and when
        client.removeAttribute(buildId, key);

        // then
        Build withRemovedAttribute = client.getSpecific(buildId);
        assertThat(withRemovedAttribute.getAttributes()).doesNotContainKey(key);
    }

    @Test
    public void shouldGetBuildConfigurationRevision() throws ClientException {
        // when
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        BuildConfigurationRevision bcRevision = client.getBuildConfigRevision(buildId);

        // then
        assertThat(bcRevision.getScmRevision()).isEqualTo("*/v0.2"); // from
                                                                     // DatabaseDataInitializer
    }

    @Test
    public void shouldGetAlignLogs() throws ClientException, IOException {
        // when
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        Optional<InputStream> stream = client.getAlignLogs(buildId);

        // then
        assertThat(stream).isPresent();
        String log = IoUtils.readStreamAsString(stream.get());
        assertThat(log).contains("alignment log"); // from DatabaseDataInitializer
    }

    @Test
    public void shouldGetBuildLogs() throws ClientException, IOException {
        // when
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        Optional<InputStream> stream = client.getBuildLogs(buildId);

        // then
        assertThat(stream).isPresent();
        String log = IoUtils.readStreamAsString(stream.get());
        assertThat(log).contains("demo log"); // from DatabaseDataInitializer
    }

    @Test
    public void shouldFailToGetSshCredentialsForUserThatDidntTrigger() {
        BuildClient client = new BuildClient(RestClientConfiguration.getConfiguration(Credentials.USER2));

        assertThatThrownBy(() -> client.getSshCredentials(buildId)).hasCauseInstanceOf(ForbiddenException.class); // 403
                                                                                                                  // means
                                                                                                                  // not
                                                                                                                  // authorized
    }

    @Test
    public void shouldFailToGetSshCredentialsForAnonymous() {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());

        assertThatThrownBy(() -> client.getSshCredentials(buildId)).hasCauseInstanceOf(NotAuthorizedException.class); // 401
                                                                                                                      // means
                                                                                                                      // not
                                                                                                                      // authenticated
    }

    @Test
    public void shouldGetByStatusAndLog() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<Build> builds = client.getAllByStatusAndLogContaining(BuildStatus.SUCCESS, "fox");
        assertThat(builds.size()).isGreaterThan(0);
        Build build = builds.iterator().next();
        logger.info("Found build:" + build.toString());
    }

    @Test
    public void shouldFailAsRegularUser() {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());

        String buildRecordId = "1";
        assertThatThrownBy(() -> client.setBuiltArtifacts(buildRecordId, Collections.emptyList()))
                .hasCauseInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> client.setDependentArtifacts(buildRecordId, Collections.emptyList()))
                .hasCauseInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> client.update(buildRecordId, Build.builder().build()))
                .hasCauseInstanceOf(ForbiddenException.class);
    }

    @Test
    public void shouldGetOldTemporaryBuild() {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());

    }

    @Test
    public void shouldReturnForbiddenCodeForPushOfBadQualityArtifact() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());
        ArtifactClient artifactClient = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact badQuality = artifactClient.create(
                Artifact.builder()
                        .artifactQuality(ArtifactQuality.DELETED)
                        .filename("builtArtifactInsert2.jar")
                        .identifier("integration-test:built-artifact:jar:1.0")
                        .targetRepository(artifactClient.getSpecific("100").getTargetRepository())
                        .md5("insert-md5-2")
                        .sha1("insert-2")
                        .sha256("insert-2")
                        .size(10L)
                        .build());

        client.setBuiltArtifacts(build2Id, Collections.singletonList(badQuality.getId()));

        assertThatThrownBy(
                () -> client.push(build2Id, BuildPushParameters.builder().reimport(true).tagPrefix("test-tag").build()))
                        .hasCauseInstanceOf(ForbiddenException.class);
    }

    @Test
    public void shouldModifyBuiltArtifactQualityLevels() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());
        String buildRecordId = build3Id;
        String REASON = "This artifact has become old enough";

        ArtifactClient artifactClient = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact newArtifact = artifactClient.create(
                Artifact.builder()
                        .artifactQuality(ArtifactQuality.NEW)
                        .filename("builtArtifactInsertNew.jar")
                        .identifier("integration-test:built-artifact-new:jar:1.0")
                        .targetRepository(artifactClient.getSpecific("100").getTargetRepository())
                        .md5("insert-md5-22")
                        .sha1("insert-22")
                        .sha256("insert-22")
                        .size(10L)
                        .build());

        client.setBuiltArtifacts(build3Id, Collections.singletonList(newArtifact.getId()));
        client.createBuiltArtifactsQualityLevelRevisions(buildRecordId, "BLACKListed", REASON);

        RemoteCollection<Artifact> artifacts = client.getBuiltArtifacts(buildRecordId);
        for (Artifact artifact : artifacts) {
            assertThat(artifact.getArtifactQuality()).isEqualTo(ArtifactQuality.BLACKLISTED);
            assertThat(artifact.getQualityLevelReason()).isEqualTo(REASON);
            assertThat(artifact.getModificationUser().getUsername()).isEqualTo("system");
        }
        Build withAttribute = client.getSpecific(buildRecordId);
        assertThat(withAttribute.getAttributes()).contains(entry(Attributes.BLACKLIST_REASON, REASON));
    }

    @Test
    public void shouldNotStandardUserModifyUnallowedQualityLevel() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        String buildRecordId = "1";
        String REASON = "This artifact has become old enough";

        assertThatThrownBy(() -> client.createBuiltArtifactsQualityLevelRevisions(buildRecordId, "BLACKListed", REASON))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldNotApplyUnknownQualityLevel() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        String buildRecordId = "1";
        String REASON = "This artifact will be marked as WHITELISTED";

        assertThatThrownBy(() -> client.createBuiltArtifactsQualityLevelRevisions(buildRecordId, "WHITELISTED", REASON))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    private Set<Integer> artifactIds(RemoteCollection<Artifact> artifacts) {
        Set<Integer> artifactIds = new HashSet<>();
        for (Artifact a : artifacts) {
            artifactIds.add(Integer.valueOf(a.getId()));
        }
        return artifactIds;
    }
}
