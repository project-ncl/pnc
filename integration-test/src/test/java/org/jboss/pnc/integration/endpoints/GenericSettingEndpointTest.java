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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class GenericSettingEndpointTest {

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

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
        assertTrue(
                Stream.of("Invocation on method", "GenericSettingProvider.setAnnouncementBanner", "is not allowed")
                        .allMatch(details::contains));
    }
}
