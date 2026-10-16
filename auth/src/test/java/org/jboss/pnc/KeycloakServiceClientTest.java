/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.mock.common.SystemConfigMock;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category({ DebugTest.class })
public class KeycloakServiceClientTest {

    @Test
    public void shouldObtainAuthToken() throws ConfigurationParseException, IOException {
        SystemConfig systemConfig = SystemConfigMock.withKeycloakServiceAccount();
        KeycloakServiceClient keycloakServiceClient = new DefaultKeycloakServiceClient(systemConfig);
        String authToken = keycloakServiceClient.getAuthToken();

        Assertions.assertThat(authToken).isNotEmpty();
    }

}
