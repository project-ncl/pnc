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
package org.jboss.pnc.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Default implementation of the ServiceAccountClient interface.
 *
 * Right now it only returns the OIDC auth value (Bearer xxx) but in the future it could be doing Basic auth (Basic xx)
 * via a switch in our configuration
 */
@ApplicationScoped
public class DefaultServiceAccountClient implements ServiceAccountClient {

    KeycloakServiceClient keycloakServiceClient;

    @Deprecated // CDI workaround
    public DefaultServiceAccountClient() {
    }

    @Inject
    public DefaultServiceAccountClient(KeycloakServiceClient keycloakServiceClient) {
        this.keycloakServiceClient = keycloakServiceClient;
    }

    @Override
    public String getAuthHeaderValue() {
        return oidcHeaderValue();
    }

    private String oidcHeaderValue() {
        return "Bearer " + keycloakServiceClient.getAuthToken();
    }
}
