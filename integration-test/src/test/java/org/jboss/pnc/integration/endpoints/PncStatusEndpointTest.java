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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.PncStatusClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class PncStatusEndpointTest {

    private final static String TEST_BANNER = "Test banner";
    private final static String ETA = Instant.now().toString();

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void setBannerWhenNotAuthenticated() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var pncStatus = PncStatus.builder().banner(TEST_BANNER).build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("NotAuthorizedException");
        assertThat(errorResponse.getErrorMessage()).isEqualTo("HTTP 401 Unauthorized");
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    public void setBannerWhenNotAuthorized() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asUser());
        var pncStatus = PncStatus.builder().banner("Test banner").build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("EJBAccessException");
        assertThat(errorResponse.getErrorMessage()).isEqualTo(
                "Insufficient privileges: the required role to access the resource is missing in the provided JWT.");
        String details = (String) errorResponse.getDetails();
        assertTrue(
                Stream.of("Invocation on method", "GenericSettingProvider.setAnnouncementBanner", "is not allowed")
                        .allMatch(details::contains));
    }

    @Test
    public void setBannerWhenBadRequest() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().isMaintenanceMode(false).eta(ETA).build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("BadRequestException");
        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Can't set ETA when maintenance mode is off and banner is null.");
    }

    @Test
    @InSequence(10)
    public void setBannerWhenAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().banner(TEST_BANNER).build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(20)
    public void getBanner() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var expectedPncStatus = PncStatus.builder().banner(TEST_BANNER).build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }
}
