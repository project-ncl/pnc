/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.rest.validation.exceptions.RepositoryViolationException;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jakub Bartecek <jbartece@redhat.com>
 */
public class BuildConfigSetRecordEndpointTest {

    private static final int CURRENT_USER = randInt(1000, 100000);

    private static final String USER_TOKEN = "token";

    @Mock
    private EndpointAuthenticationProvider authProvider;

    @Mock
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    @Mock
    private BuildRecordProvider buildRecordProvider;

    @Mock
    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private BuildConfigSetRecordEndpoint endpoint;

    private User user;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.user = mock(User.class);
        when(user.getId()).thenReturn(CURRENT_USER);
        when(user.getLoginToken()).thenReturn(USER_TOKEN);
        when(authProvider.getCurrentUser(any())).thenReturn(user);
    }

    @Test
    public void shouldPerformCallbackAfterDeletion() throws RepositoryViolationException, ValidationException {
        // given
        final int buildId = 88;
        final String buildIdString = new Integer(buildId).toString();
        final String callbackUrl = "http://localhost:8088/callback";

        WireMockServer wireMockServer = new WireMockServer(8088);
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/callback")).willReturn(aResponse().withStatus(200)));

        given(temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(eq(buildId), eq(USER_TOKEN), any
                ())).willAnswer(invocation -> {
            Result result = new Result(buildIdString, Result.Status.SUCCESS, "BuildConfigSetRecord was deleted " +
                    "successfully");

            ((Consumer<Result>) invocation.getArgument(2)).accept(result);
            return true;
        });

        // when
        Response response = endpoint.delete(buildId, callbackUrl);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/callback")).withRequestBody(matchingJsonPath("$.id",
                equalTo(buildIdString))));
        wireMockServer.stop();
    }
}
