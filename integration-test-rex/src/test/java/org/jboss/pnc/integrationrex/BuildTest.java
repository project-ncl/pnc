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
package org.jboss.pnc.integrationrex;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.auth.KeycloakClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.integrationrex.mock.BPMResultsMock;
import org.jboss.pnc.integrationrex.setup.RestClientConfiguration;
import org.jboss.pnc.integrationrex.utils.BuildUtils;
import org.jboss.pnc.integrationrex.utils.ResponseUtils;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.restclient.AdvancedBuildClient;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jboss.pnc.integrationrex.WireMockUtils.baseBPMWebhook;
import static org.jboss.pnc.integrationrex.WireMockUtils.defaultConfiguration;
import static org.jboss.pnc.integrationrex.WireMockUtils.response200;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.BASE_REST_PATH;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.withBearerToken;

@RunAsClient
@RunWith(Arquillian.class)
@Category({ ContainerTest.class })
public class BuildTest extends RemoteServices {

    private static final Logger logger = LoggerFactory.getLogger(BuildTest.class);

    private BuildConfigurationClient buildConfigurationClient;

    private GroupConfigurationClient groupConfigurationClient;

    private BuildClient buildClient;

    private BuildUtils buildUtils;

    private static BPMWireMock bpm;

    @BeforeClass
    public static void startBPM() {
        bpm = new BPMWireMock(8088);
    }

    @AfterClass
    public static void stopBPM() throws IOException {
        if (bpm != null) {
            bpm.close();
        }
    }

    @Before
    public void beforeEach() throws ExecutionException, InterruptedException {

        String token = KeycloakClient
                .getAuthTokensBySecret(authServerUrl, keycloakRealm, "test-user", "test-pass", "pnc", "", false)
                .getToken();

        buildClient = new AdvancedBuildClient(withBearerToken(token));
        buildConfigurationClient = new BuildConfigurationClient(withBearerToken(token));
        groupConfigurationClient = new GroupConfigurationClient(withBearerToken(token));
        buildUtils = new BuildUtils(buildClient, new GroupBuildClient(withBearerToken(token)));
    }

    @Test
    public void testThatBuildQueueSizeIsSet() {
        Configuration conf = RestClientConfiguration.asAnonymous();

        given().baseUri(conf.getProtocol() + "://" + conf.getHost() + ":" + conf.getPort())
                .basePath(BASE_REST_PATH)
                .when()
                .get("/debug/build-queue/size")
                .then()
                .statusCode(200)
                .body("number", equalTo(10)); // value from Rex set from pnc-config.json
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() throws ClientException {
        // with
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll().iterator().next();

        // when
        Build build = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getPersistentParameters(true));
        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils
                .waitSynchronouslyFor(() -> buildUtils.buildToFinish(build.getId(), isIn, null), 300, TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerGroupBuildAndFinishWithoutProblems() throws ClientException {
        // given
        GroupConfiguration groupConfig = groupConfigurationClient.getAll().iterator().next();

        // when
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);

        GroupBuild groupBuild = groupConfigurationClient.trigger(
                groupConfig.getId(),
                groupBuildParameters,
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());
        assertThat(groupBuild).isNotNull().extracting("id").isNotNull().isNotEqualTo("");
        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerBuildWithADependencyAndFinishWithoutProblems() throws ClientException {
        // given - A BC with a dependency on pnc-1.0.0.DR1
        BuildConfiguration buildConfigurationParent = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==dependency-analysis-1.3"))
                .iterator()
                .next();

        // Update dependency
        BuildConfiguration buildConfigurationChild = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-1.0.0.DR1"))
                .iterator()
                .next();
        BuildConfiguration updatedBuildConfigurationChild = buildConfigurationChild.toBuilder()
                .description(buildConfigurationChild.getDescription() + ".")
                .build();

        buildConfigurationClient.update(buildConfigurationChild.getId(), updatedBuildConfigurationChild);

        // The update of the description should not have changed the lastModificationDate
        assertThat(buildConfigurationChild.getModificationTime())
                .isEqualTo(updatedBuildConfigurationChild.getModificationTime());

        // when
        Build build = buildConfigurationClient
                .trigger(buildConfigurationParent.getId(), buildUtils.getBuildParameters(false, true));

        BuildsFilterParameters parameters = new BuildsFilterParameters();
        parameters.setRunning(true);
        RemoteCollection<Build> childBuildCol = buildConfigurationClient
                .getBuilds(buildConfigurationChild.getId(), parameters);

        Build childBuild = childBuildCol.getAll().iterator().next();
        // then
        assertThat(childBuildCol.size()).isEqualTo(1);
        assertThat(buildConfigurationParent.getDependencies().size()).isEqualTo(1);

        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils
                .waitSynchronouslyFor(() -> buildUtils.buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);

        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(childBuild.getId(), isIn, null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerGroupBuildWithBCInRevisionAndFinishWithoutProblems() throws ClientException {
        // given
        GroupConfiguration groupConfiguration = groupConfigurationClient.getAll().iterator().next();
        assertThat(groupConfiguration.getBuildConfigs()).isNotEmpty();

        List<BuildConfigurationRevisionRef> buildConfigurationRevisions = new ArrayList<>();
        BuildConfigurationRevision buildConfigurationRevision = BuildConfigurationRevision.builder()
                .id(groupConfiguration.getBuildConfigs().keySet().iterator().next())
                .rev(1)
                .name(groupConfiguration.getName())
                .build();
        buildConfigurationRevisions.add(buildConfigurationRevision);

        GroupBuildRequest groupConfigWithAuditedBCsRest = GroupBuildRequest.builder()
                .buildConfigurationRevisions(buildConfigurationRevisions)
                .build();
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);

        // when
        GroupBuild groupBuild = groupConfigurationClient
                .trigger(groupConfiguration.getId(), groupBuildParameters, groupConfigWithAuditedBCsRest);
        // then
        assertThat(groupBuild).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldRejectGroupBuildWithNoRebuildsRequired() throws ClientException {
        // given
        GroupConfiguration groupConfig = groupConfigurationClient.getAll().iterator().next();

        // and after one build is done
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);
        GroupBuild groupBuild1 = groupConfigurationClient.trigger(
                groupConfig.getId(),
                groupBuildParameters,
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());
        assertThat(groupBuild1).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild1.getId(), EnumSet.of(BuildStatus.SUCCESS), null),
                15,
                TimeUnit.SECONDS);

        // when next build is triggered
        GroupBuild groupBuild2 = groupConfigurationClient.trigger(
                groupConfig.getId(),
                new GroupBuildParameters(),
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());

        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.SUCCESS, BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild2.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldBuildTemporaryBuildAndNotAssignItToMilestone() throws ClientException {
        // BC pnc-1.0.0.DR1 is assigned to a product version containing an active product milestone see
        // DatabaseDataInitializer#initiliazeProjectProductData
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-1.0.0.DR1"))
                .iterator()
                .next();

        // when

        Build build = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getTemporaryParameters(true));

        // then

        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        ResponseUtils.waitSynchronouslyFor(() -> buildUtils.buildToFinish(build.getId()), 15, TimeUnit.SECONDS);

        Build updatedBuild = buildClient.getSpecific(build.getId());
        assertThat(updatedBuild.getProductMilestone()).isNull();
    }

    @Test
    public void shouldTriggerPersistentWithoutForceAfterTemporaryOnTheSameRev() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + " clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        updatedConfiguration = buildConfigurationClient.getSpecific(updatedConfiguration.getId());
        assertThat(oldLastModDate).isNotEqualTo(updatedConfiguration.getModificationTime());
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(build.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        Build afterTempPersistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(afterTempPersistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    // NCL-5192
    // Replicates NCL-5192 through explicit dependency instead of implicit
    @Test
    public void dontRebuildTemporaryBuildWhenThereIsNewerPersistentOnSameRev()
            throws ClientException, InterruptedException {
        BuildConfiguration parent = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-build-agent-0.4"))
                .iterator()
                .next();
        BuildConfiguration dependency = buildConfigurationClient.getAll(Optional.empty(), Optional.of("name==termd"))
                .iterator()
                .next();

        Instant oldLastModDateParent = parent.getModificationTime();
        BuildConfiguration updatedParent = parent.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + "  clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedParent.getId(), updatedParent);
        updatedParent = buildConfigurationClient.getSpecific(updatedParent.getId());
        assertThat(oldLastModDateParent).isNotEqualTo(updatedParent.getModificationTime());

        Instant oldLastModDateDependency = dependency.getModificationTime();
        BuildConfiguration updatedDependency = dependency.toBuilder()
                .description("Random Description so it rebuilds")
                .buildScript("mvn" + "   clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedDependency.getId(), updatedDependency);
        updatedDependency = buildConfigurationClient.getSpecific(updatedDependency.getId());
        assertThat(oldLastModDateDependency).isNotEqualTo(updatedDependency.getModificationTime());

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build temporary builds (parent and dependency) on new revision
        Build temporaryBuild = buildConfigurationClient.trigger(parent.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(temporaryBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
        Thread.sleep(1L);
        // Build persistent build of dependency on the same revision
        Build dependencyPersistentBuild = buildConfigurationClient
                .trigger(dependency.getId(), buildUtils.getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(dependencyPersistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Build temporary build of parent and check it gets REJECTED even if it's dependency has newer record
        // (in this case temp build should ignore persistent one)
        Build finalRecord = buildConfigurationClient.trigger(parent.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(finalRecord.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldRejectAfterBuildingTwoTempBuildsOnSameRevision() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + "   clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);

        Build temporaryBuild = buildConfigurationClient
                .trigger(updatedConfiguration.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(temporaryBuild.getId(), EnumSet.of(BuildStatus.SUCCESS), null),
                15,
                TimeUnit.SECONDS);

        Build secondTempBuild = buildConfigurationClient
                .trigger(updatedConfiguration.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(
                        secondTempBuild.getId(),
                        EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED),
                        EnumSet.of(BuildStatus.SUCCESS, BuildStatus.REJECTED)),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldNotTriggerANewPersistentBuildWithoutForceIfOnlyDescriptionChanged() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build persistent builds (parent and dependency) on new revision
        Build persistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getPersistentParameters(true));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(persistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that persistent build will be first on this revision")
                .build();

        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        assertThat(oldLastModDate).isEqualTo(updatedConfiguration.getModificationTime());

        Build build2 = buildConfigurationClient
                .trigger(updatedConfiguration.getId(), buildUtils.getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(build2.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldNotTriggerANewTemporaryBuildWithoutForceIfOnlyDescriptionChanged() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build temporary builds (parent and dependency) on new revision
        Build persistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getTemporaryParameters(true));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(persistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .build();

        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        assertThat(oldLastModDate).isEqualTo(updatedConfiguration.getModificationTime());

        Build build2 = buildConfigurationClient
                .trigger(updatedConfiguration.getId(), buildUtils.getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(build2.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldHaveNoRebuildCauseFilled() throws Exception {
        // with
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll().iterator().next();

        // when #1
        Build build = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getPersistentParameters(true));
        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils
                .waitSynchronouslyFor(() -> buildUtils.buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);

        // when #2
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.SUCCESS, BuildStatus.FAILED);
        Build rebuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), buildUtils.getBuildParameters(false, false));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.buildToFinish(rebuild.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), isNotIn),
                15,
                TimeUnit.SECONDS);

        // then
        Build refresh = buildClient.getSpecific(rebuild.getId());
        assertThat(refresh.getNoRebuildCause()).isNotNull().extracting("id").isEqualTo(build.getId());
    }

    @Slf4j
    public static class BPMWireMock implements Closeable {

        private final WireMockServer wireMockServer;

        public BPMWireMock(int port) {
            wireMockServer = new WireMockServer(defaultConfiguration(port));

            wireMockServer.stubFor(
                    any(urlMatching(".*")).willReturn(response200())
                            .withPostServeAction(
                                    "webhook",
                                    baseBPMWebhook().withBody(BPMResultsMock.mockBuildResultSuccess())
                                            .withFixedDelay(250)));
            wireMockServer.start();
        }

        @Override
        public void close() throws IOException {
            wireMockServer.stop();
        }

    }

}
