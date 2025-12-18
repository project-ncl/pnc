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

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Condition;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupBuildProviderTest extends AbstractBase32LongIDProviderTest<BuildConfigSetRecord> {

    private static final int CURRENT_USER = randInt(1000, 100000);

    private static final String USER_TOKEN = "token";

    @Mock
    private BuildConfigSetRecordRepository repository;

    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;

    @Mock
    private BuildCoordinator buildCoordinator;

    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    @Mock
    private KeycloakServiceClient keycloakServiceClient;

    @Spy
    @InjectMocks
    protected GroupBuildProviderImpl provider;

    private BuildConfigSetRecord bcsr = prepareBCSetRecord(mockBuildConfigSet(1, "Fort Minor"));

    @Before
    public void fill() {
        final BuildConfigSetRecord a = prepareBCSetRecord(mockBuildConfigSet(2, "10% luck"));
        final BuildConfigSetRecord b = prepareBCSetRecord(mockBuildConfigSet(3, "20% skill"));
        final BuildConfigSetRecord c = prepareBCSetRecord(mockBuildConfigSet(4, "15% concentrated power of will"));
        final BuildConfigSetRecord d = prepareBCSetRecord(mockBuildConfigSet(5, "5% pleasure"));
        final BuildConfigSetRecord e = prepareBCSetRecord(mockBuildConfigSet(6, "50% pain"));

        List<BuildConfigSetRecord> records = new ArrayList<>(
                Arrays.asList(new BuildConfigSetRecord[] { a, b, c, d, e, bcsr }));
        fillRepository(records);

        when(keycloakServiceClient.getAuthToken()).thenReturn(USER_TOKEN);
    }

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<BuildConfigSetRecord, Base32LongID> repository() {
        return repository;
    }

    @Test
    public void testGetSpecific() {
        GroupBuild groupBuild = provider.getSpecific(bcsr.getId().toString());

        assertThat(groupBuild).isNotNull();
        assertThat(groupBuild.getId()).isEqualTo(bcsr.getId().toString());
        assertThat(groupBuild.getGroupConfig().getId()).isEqualTo(bcsr.getBuildConfigurationSet().getId().toString());
    }

    @Test
    public void testGetAll() {
        Page<GroupBuild> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(6)
                .haveExactly(
                        1,
                        new Condition<>(
                                gB -> gB.getGroupConfig().getName().equals(bcsr.getBuildConfigurationSet().getName()),
                                "GroupBuild present"));
    }

    @Test
    public void testStore() {
        assertThatThrownBy(() -> provider.store(mock(GroupBuild.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testCancel() throws CoreException {
        // When
        provider.cancel(bcsr.getId().toString());
        // Then
        verify(buildCoordinator, times(1)).cancelSet(bcsr.getId());
    }

    @Test
    public void shouldProvideCallbackOnDeletion() throws Exception {
        // given
        final Base32LongID buildId = new Base32LongID(88L);
        final String buildIdString = buildId.getId();
        final String callbackUrl = "http://localhost:8088/callback";
        WireMockServer wireMockServer = new WireMockServer(8088);
        try {
            wireMockServer.start();
            wireMockServer.stubFor(post(urlEqualTo("/callback")).willReturn(aResponse().withStatus(200)));

            given(
                    temporaryBuildsCleanerAsyncInvoker
                            .deleteTemporaryBuildConfigSetRecord(eq(buildId), eq(USER_TOKEN), any()))
                                    .willAnswer(invocation -> {
                                        Result result = new Result(
                                                buildIdString,
                                                ResultStatus.SUCCESS,
                                                "BuildConfigSetRecord was deleted " + "successfully");

                                        ((Consumer<Result>) invocation.getArgument(2)).accept(result);
                                        return true;
                                    });

            // when
            boolean result = provider.delete(buildIdString, callbackUrl);

            // then
            assertThat(result).isTrue();
            wireMockServer.verify(
                    1,
                    postRequestedFor(urlEqualTo("/callback"))
                            .withRequestBody(matchingJsonPath("$.id", equalTo(buildIdString))));
        } finally {
            wireMockServer.stop();
        }
    }

    private BuildConfigSetRecord prepareBCSetRecord(BuildConfigurationSet buildConfigurationSet) {
        final BuildConfigSetRecord record = BuildConfigSetRecord.Builder.newBuilder()
                .id(getNextId())
                .buildConfigurationSet(buildConfigurationSet)
                .startTime(Date.from(Instant.now()))
                .user(mockUser())
                .temporaryBuild(false)
                .build();
        buildConfigurationSet.addBuildConfigSetRecord(record);
        return record;
    }

    private User mockUser() {
        return User.Builder.newBuilder()
                .id(1)
                .username("jsmith")
                .firstName("John")
                .lastName("Smith")
                .email("jsmith@gmail.com")
                .build();
    }

    private BuildConfigurationSet mockBuildConfigSet(int id, String name) {
        return BuildConfigurationSet.Builder.newBuilder()
                .id(id)
                .name(name)
                .archived(false)
                .buildConfigurations(new HashSet<>())
                .build();
    }
}
