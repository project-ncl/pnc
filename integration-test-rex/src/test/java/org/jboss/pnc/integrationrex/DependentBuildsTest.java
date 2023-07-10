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
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.integrationrex.mock.BPMResultsMock;
import org.jboss.pnc.integrationrex.utils.BuildUtils;
import org.jboss.pnc.integrationrex.utils.ResponseUtils;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.restclient.websocket.VertxWebSocketClient;
import org.jboss.pnc.restclient.websocket.WebSocketClient;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.jboss.pnc.integrationrex.WireMockUtils.baseBPMWebhook;
import static org.jboss.pnc.integrationrex.WireMockUtils.defaultConfiguration;
import static org.jboss.pnc.integrationrex.WireMockUtils.response200;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.NOTIFICATION_PATH;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.withBearerToken;

@RunAsClient
@RunWith(Arquillian.class)
@Category({ ContainerTest.class })
public class DependentBuildsTest extends RemoteServices {

    private GroupConfigurationClient groupConfigurationClient;

    private GroupBuildClient groupBuildClient;

    private BuildUtils buildUtils;

    private BPMWireMock bpm;

    private static final String PNC_SOCKET_URL = "ws://localhost:8080" + NOTIFICATION_PATH;
    WebSocketClient wsClient = new VertxWebSocketClient();
    private BuildClient buildClient;
    private BuildConfigurationClient buildConfigurationClient;

    @Before
    public void beforeEach() throws ExecutionException, InterruptedException {
        String token = KeycloakClient
                .getAuthTokensBySecret(authServerUrl, keycloakRealm, "test-user", "test-pass", "pnc", "", false)
                .getToken();

        groupConfigurationClient = new GroupConfigurationClient(withBearerToken(token));
        groupBuildClient = new GroupBuildClient(withBearerToken(token));
        buildConfigurationClient = new BuildConfigurationClient(withBearerToken(token));

        wsClient.connect(PNC_SOCKET_URL).get();

        buildClient = new BuildClient(withBearerToken(token));
        buildUtils = new BuildUtils(buildClient, new GroupBuildClient(withBearerToken(token)));
        bpm = new BPMWireMock(8088);
    }

    @After
    public void afterEach() throws IOException {
        bpm.close();
        wsClient.disconnect();
    }

    @Test
    public void shouldBuildProjectWithDependencies() throws RemoteResourceException {
        BuildConfigurationCreator buildConfigurationCreator = new BuildConfigurationCreator(
                authServerUrl,
                keycloakRealm);
        BuildConfiguration bc5 = buildConfigurationCreator.newBuildConfiguration("BC5");
        BuildConfiguration bc4 = buildConfigurationCreator.newBuildConfiguration("BC4", Set.of(bc5));
        BuildConfiguration bc3 = buildConfigurationCreator.newBuildConfiguration("BC3", Set.of(bc5));
        BuildConfiguration bc2 = buildConfigurationCreator.newBuildConfiguration("BC2", Set.of(bc3, bc4));
        BuildConfiguration bc1 = buildConfigurationCreator.newBuildConfiguration("BC1", Set.of(bc2, bc3));

        GroupConfiguration group = buildConfigurationCreator
                .newGroupConfiguration("group-pass", Set.of(bc1, bc2, bc3, bc4, bc5));

        GroupBuildParameters params = new GroupBuildParameters();
        params.setRebuildMode(RebuildMode.FORCE);
        GroupBuildRequest request = GroupBuildRequest.builder().build();
        GroupBuild groupBuild = groupConfigurationClient.trigger(group.getId(), params, request);

        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);

        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                10,
                TimeUnit.SECONDS);

        // make sure all builds completed
        RemoteCollection<Build> builds = groupBuildClient.getBuilds(groupBuild.getId(), new BuildsFilterParameters());
        for (Build build : builds) {
            ResponseUtils.waitSynchronouslyFor(
                    () -> buildUtils.buildToFinish(build.getId(), EnumSet.of(BuildStatus.SUCCESS), null),
                    10,
                    TimeUnit.SECONDS);
        }
    }

    @Test
    public void shouldRejectBuildWithFailedDependency() throws RemoteResourceException, InterruptedException {
        BuildConfigurationCreator buildConfigurationCreator = new BuildConfigurationCreator(
                authServerUrl,
                keycloakRealm);
        BuildConfiguration bc11 = buildConfigurationCreator.newBuildConfiguration("BC11-fail");
        BuildConfiguration bc10 = buildConfigurationCreator.newBuildConfiguration("BC10", Set.of(bc11));

        GroupConfiguration group = buildConfigurationCreator.newGroupConfiguration("group-fail", Set.of(bc10, bc11));

        GroupBuildParameters params = new GroupBuildParameters();
        params.setRebuildMode(RebuildMode.FORCE);
        GroupBuildRequest request = GroupBuildRequest.builder().build();
        GroupBuild groupBuild = groupConfigurationClient.trigger(group.getId(), params, request);

        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.FAILED);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);

        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                10,
                TimeUnit.SECONDS);

        BuildsFilterParameters parameters = new BuildsFilterParameters();
        parameters.setLatest(true);
        parameters.setRunning(false);
        Build build10 = buildConfigurationClient.getBuilds(bc10.getId(), parameters).iterator().next();
        Build build11 = buildConfigurationClient.getBuilds(bc11.getId(), parameters).iterator().next();

        Assert.assertEquals(BuildStatus.REJECTED_FAILED_DEPENDENCIES, build10.getStatus());
        Assert.assertEquals(BuildStatus.FAILED, build11.getStatus());
    }

    @Test
    public void shouldRejectBuildWithFailedTransitiveDependency() throws RemoteResourceException {
        BuildConfigurationCreator buildConfigurationCreator = new BuildConfigurationCreator(
                authServerUrl,
                keycloakRealm);
        BuildConfiguration bc22 = buildConfigurationCreator.newBuildConfiguration("BC22-fail");
        BuildConfiguration bc21 = buildConfigurationCreator.newBuildConfiguration("BC21", Set.of(bc22));
        BuildConfiguration bc20 = buildConfigurationCreator.newBuildConfiguration("BC20", Set.of(bc21));

        GroupConfiguration group = buildConfigurationCreator
                .newGroupConfiguration("group-fail-transitive", Set.of(bc20, bc21, bc22));

        GroupBuildParameters params = new GroupBuildParameters();
        params.setRebuildMode(RebuildMode.FORCE);
        GroupBuildRequest request = GroupBuildRequest.builder().build();
        GroupBuild groupBuild = groupConfigurationClient.trigger(group.getId(), params, request);

        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.FAILED);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);

        ResponseUtils.waitSynchronouslyFor(
                () -> buildUtils.groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                10,
                TimeUnit.SECONDS);

        BuildsFilterParameters parameters = new BuildsFilterParameters();
        parameters.setLatest(true);
        parameters.setRunning(false);
        Build build20 = buildConfigurationClient.getBuilds(bc20.getId(), parameters).iterator().next();
        Build build21 = buildConfigurationClient.getBuilds(bc21.getId(), parameters).iterator().next();
        Build build22 = buildConfigurationClient.getBuilds(bc22.getId(), parameters).iterator().next();

        Assert.assertEquals(BuildStatus.REJECTED_FAILED_DEPENDENCIES, build20.getStatus());
        Assert.assertEquals(BuildStatus.REJECTED_FAILED_DEPENDENCIES, build21.getStatus());
        Assert.assertEquals(BuildStatus.FAILED, build22.getStatus());
    }

    @Slf4j
    public static class BPMWireMock implements Closeable {

        private final WireMockServer wireMockServer;

        public BPMWireMock(int port) {
            wireMockServer = new WireMockServer(defaultConfiguration(port));

            wireMockServer.stubFor(
                    any(urlMatching(".*")).atPriority(1)
                            .withRequestBody(
                                    matchingJsonPath(
                                            "$.payload.initData.task.processParameters.buildExecutionConfiguration.name",
                                            containing("fail")))
                            .willReturn(response200())
                            .withPostServeAction(
                                    "webhook",
                                    baseBPMWebhook().withBody(BPMResultsMock.mockBuildResultFailed())
                                            .withFixedDelay(250)));

            wireMockServer.stubFor(
                    any(urlMatching(".*")).atPriority(100)
                            .willReturn(response200())
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
