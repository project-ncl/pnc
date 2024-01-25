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
    private final static String AFTER_MAINTENANCE_BANNER = "Wuw, wuw, did we already solve that? Oooh yes, it was that lightning fast.";
    private final static String ETA_OF_MAINTENANCE = Instant.now().toString();

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void setBannerWhenNotAuthenticated() {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var pncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).build();

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
    @InSequence(10)
    public void setBannerWhenAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(20)
    public void getBannerBeforeMaintenance() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asAnonymous());
        var expectedPncStatus = PncStatus.builder().banner(BEFORE_MAINTENANCE_BANNER).eta(Strings.EMPTY).build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }

    @Test
    @InSequence(30)
    public void activateMaintenanceModeWhenNotAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asUser());
        var pncStatus = PncStatus.builder()
                .banner(BEFORE_MAINTENANCE_BANNER)
                .eta(ETA_OF_MAINTENANCE)
                .isMaintenanceMode(true)
                .build();

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
                Stream.of("Invocation on method", "GenericSettingProvider.activateMaintenanceMode", "is not allowed")
                        .allMatch(details::contains));
    }

    @Test
    @InSequence(30)
    public void activateMaintenanceModeWhenAuthorized() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var pncStatus = PncStatus.builder()
                .banner(MAINTENANCE_BANNER)
                .eta(ETA_OF_MAINTENANCE)
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
                .eta(ETA_OF_MAINTENANCE)
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
        var pncStatus = PncStatus.builder().banner(AFTER_MAINTENANCE_BANNER).isMaintenanceMode(false).build();

        // when
        client.setPncStatus(pncStatus);
    }

    @Test
    @InSequence(60)
    public void getBannerAfterMaintenance() throws RemoteResourceException {
        // given
        var client = new PncStatusClient(RestClientConfiguration.asSystem());
        var expectedPncStatus = PncStatus.builder()
                .banner(AFTER_MAINTENANCE_BANNER)
                .eta(Strings.EMPTY)
                .isMaintenanceMode(false)
                .build();

        // when
        PncStatus actualPncStatus = client.getPncStatus();

        // then
        assertThat(actualPncStatus).isEqualTo(expectedPncStatus);
    }
}
