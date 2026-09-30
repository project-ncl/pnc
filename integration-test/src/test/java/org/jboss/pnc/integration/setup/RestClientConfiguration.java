/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.setup;

import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.rest.endpoints.notifications.NotificationsEndpoint;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestClientConfiguration {
    public static final String BASE_PATH = "/pnc-rest";
    public static final String BASE_REST_PATH = BASE_PATH + "/v2";
    public static final String NOTIFICATION_PATH = BASE_PATH + NotificationsEndpoint.ENDPOINT_PATH;

    public static Configuration getConfiguration(Credentials credentials) {
        Configuration.ConfigurationBuilder builder = Configuration.builder();
        if (!Credentials.NONE.equals(credentials)) {
            builder.basicAuth(credentials.passCredentials(Configuration.BasicAuth::new));
        }
        builder.protocol("http");
        builder.host("localhost");
        builder.port(IntegrationTestEnv.getHttpPort());

        builder.addDefaultMdcToHeadersMappings();

        return builder.build();
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

}
