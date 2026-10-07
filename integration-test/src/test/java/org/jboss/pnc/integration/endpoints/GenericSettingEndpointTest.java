/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.GenericSettingClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class GenericSettingEndpointTest {

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    /**
     * TODO: Remove once {@link org.jboss.pnc.rest.api.endpoints.GenericSettingEndpoint#setAnnouncementBanner(String)}
     * is removed
     */
    @Test
    public void testSetBannerWhenUnauthorized() {
        // given
        var client = new GenericSettingClient(RestClientConfiguration.asUser());

        // when + then
        RemoteResourceException remoteResourceException = assertThrows(
                RemoteResourceException.class,
                () -> client.setAnnouncementBanner("Test banner"));
        ErrorResponse errorResponse = remoteResourceException.getResponse().get();
        assertThat(errorResponse.getErrorType()).isEqualTo("EJBAccessException");
        assertThat(errorResponse.getErrorMessage()).isEqualTo(
                "Insufficient privileges: the required role to access the resource is missing in the provided JWT.");
        String details = (String) errorResponse.getDetails();
        assertThat(details).isEqualTo("Only users with the pnc-users-admin role are allowed to perform this operation");
    }
}
