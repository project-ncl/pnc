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
package org.jboss.pnc.integration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.integration.BuildRecordRestTest;
import org.jboss.pnc.integration.utils.AuthResource;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractRestClient {

    protected static AuthenticationProvider authProvider;
    protected static String access_token = "no-auth";

    protected static boolean authInitialized = false;

    protected ObjectMapper jsonMapper = new ObjectMapper();

    protected AbstractRestClient() {

    }

    protected void initAuth() throws IOException, ConfigurationParseException {
        if (AuthResource.authEnabled() && !authInitialized) {
            AuthenticationModuleConfig config = new Configuration().getModuleConfig(AuthenticationModuleConfig.class);
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
            authInitialized = true;
        }
    }

}
