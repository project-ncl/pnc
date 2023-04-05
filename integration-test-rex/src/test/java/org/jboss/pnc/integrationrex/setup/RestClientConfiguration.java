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
package org.jboss.pnc.integrationrex.setup;

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.rest.endpoints.notifications.NotificationsEndpoint;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestClientConfiguration {
    public static final String BASE_PATH = "/pnc-rest";
    public static final String BASE_REST_PATH = BASE_PATH + "/v2";
    public static final String NOTIFICATION_PATH = BASE_PATH + NotificationsEndpoint.ENDPOINT_PATH;

    private static Configuration getConfiguration(Credentials credentials) {
        Configuration.ConfigurationBuilder builder = getConfigurationBuilder();
        if (!Credentials.NONE.equals(credentials)) {
            builder.basicAuth(credentials.passCredentials(Configuration.BasicAuth::new));
        }
        return builder.build();
    }

    private static Configuration.ConfigurationBuilder getConfigurationBuilder() {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        builder.protocol("http");
        builder.host("localhost");
        builder.port(IntegrationTestEnv.getHttpPort());

        builder.addDefaultMdcToHeadersMappings();

        return builder;
    }

    public static Configuration asAnonymous() {
        return getConfiguration(Credentials.NONE);
    }

    public static Configuration asUser() {
        return getConfiguration(Credentials.USER);
    }

    public static Configuration asSystem() {
        return getConfiguration(Credentials.SYSTEM_USER);
    }

    public static Configuration withBearerToken(String token) {
        Configuration.ConfigurationBuilder builder = getConfigurationBuilder();
        builder.bearerTokenSupplier(() -> token);
        return builder.build();
    }
}
