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
package org.jboss.pnc;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.auth.DefaultKeycloakServiceClient;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.mock.common.SystemConfigMock;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

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
