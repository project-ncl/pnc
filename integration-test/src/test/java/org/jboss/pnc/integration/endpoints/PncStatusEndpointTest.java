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
import org.jboss.util.Strings;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class PncStatusEndpointTest {

    private final static String BEFORE_MAINTENANCE_BANNER = "There will be a maintenance. Noone knows when.";
    private final static String MAINTENANCE_BANNER = "We're doing some nasty stuff outta here, you better watch out.";
    private final static long SECONDS_IN_DAY = 60 * 60 * 12;
    private final static Instant END_OF_MAINTENANCE_ETA = Instant.now()
            .truncatedTo(ChronoUnit.MILLIS)
            .plusSeconds(SECONDS_IN_DAY);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldFailWhenNotAuthenticated() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var pncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).isMaintenanceMode(false).build();

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
    public void shouldFailWhenNotAuthorized() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asUser());
        var pncStatus = PncStatus.builder().banner("Test banner").isMaintenanceMode(false).build();

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
    public void shouldFailOnBadRequest() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().isMaintenanceMode(false).eta(END_OF_MAINTENANCE_ETA).build();

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
    public void shouldFailOnBlankBanner() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().banner(Strings.EMPTY).isMaintenanceMode(false).build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("BadRequestException");
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Banner cannot be blank.");
    }

    @Test
    public void shouldFailWhenEtaInPast() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder()
                .banner(BEFORE_MAINTENANCE_BANNER)
                .eta(Instant.now().minusSeconds(SECONDS_IN_DAY))
                .isMaintenanceMode(false)
                .build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("VALIDATION");
        assertTrue(
                Stream.of("Constraint violation", "eta", "must be a future date")
                        .allMatch(errorResponse.getErrorMessage()::contains));
    }

    @Test
    public void shouldFailWhenMaintenanceModeNotPresent() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).build();

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setPncStatus(pncStatus));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("VALIDATION");
        assertTrue(
                Stream.of("Constraint violation", "isMaintenanceMode", "must not be null")
                        .allMatch(errorResponse.getErrorMessage()::contains));
    }

    @Test
    public void maintenanceModeShouldBeOffWhenNotStored() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var expectedPncStatus = PncStatus.builder().isMaintenanceMode(Boolean.FALSE).build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }

    @Test
    @InSequence(10)
    public void setBannerWhenAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).isMaintenanceMode(false).build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(20)
    public void getBanner() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var expectedPncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).isMaintenanceMode(false).build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }

    @Test
    @InSequence(30)
    public void activateMaintenanceMode() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder()
                .banner(MAINTENANCE_BANNER)
                .eta(END_OF_MAINTENANCE_ETA)
                .isMaintenanceMode(true)
                .build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(40)
    public void getBannerDuringMaintenance() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var expectedPncStatus = PncStatus.builder()
                .banner(MAINTENANCE_BANNER)
                .eta(END_OF_MAINTENANCE_ETA)
                .isMaintenanceMode(true)
                .build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }

    @Test
    @InSequence(50)
    public void deactivateMaintenanceModeWhenAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().isMaintenanceMode(false).build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(60)
    public void getBannerAfterMaintenance() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var expectedPncStatus = PncStatus.builder()
                // banner should be cleared
                // eta should be cleared
                .isMaintenanceMode(false)
                .build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }
}
