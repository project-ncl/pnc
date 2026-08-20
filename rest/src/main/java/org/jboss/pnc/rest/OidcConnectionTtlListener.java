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
package org.jboss.pnc.rest;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.http.oidc.HttpClientBuilder;
import org.wildfly.security.http.oidc.OidcClientConfiguration;
import org.wildfly.security.http.oidc.OidcClientConfigurationBuilder;
import org.wildfly.security.http.oidc.OidcClientContext;
import org.wildfly.security.http.oidc.OidcJsonConfiguration;

/**
 * Configures a connection TTL on Elytron's OIDC HttpClient so that pooled connections are replaced before a firewall/LB
 * can kill them for being idle. Must be declared in web.xml AFTER OidcConfigurationServletListener.
 *
 * Elytron's HttpClientBuilder has setConnectionTimeToLive() but the build(OidcJsonConfiguration) path never calls it,
 * so pooled connections default to infinite TTL. This listener re-builds the HttpClient with a finite TTL.
 *
 * Note that on Elytron 2.x / EAP 8 we can configure the ttl via a configuration setting, so this class will not be
 * needed anymore and can be removed.
 */
public class OidcConnectionTtlListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(OidcConnectionTtlListener.class);

    private static final long CONNECTION_TTL_SECONDS = 30;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        OidcClientContext clientContext = (OidcClientContext) servletContext
                .getAttribute(OidcClientContext.class.getName());

        if (clientContext == null) {
            logger.debug("No OidcClientContext found, skipping connection TTL configuration");
            return;
        }

        try {
            Field configField = OidcClientContext.class.getDeclaredField("oidcClientConfig");
            configField.setAccessible(true);
            OidcClientConfiguration oidcConfig = (OidcClientConfiguration) configField.get(clientContext);

            if (oidcConfig == null) {
                logger.debug("No OidcClientConfiguration found, skipping connection TTL configuration");
                return;
            }

            InputStream is = servletContext.getResourceAsStream("/WEB-INF/oidc.json");
            if (is == null) {
                logger.warn("Cannot find /WEB-INF/oidc.json, skipping connection TTL configuration");
                return;
            }

            OidcJsonConfiguration jsonConfig;
            try {
                jsonConfig = OidcClientConfigurationBuilder.loadOidcJsonConfiguration(is);
            } finally {
                is.close();
            }

            oidcConfig.setClient(
                    new HttpClientBuilder().setConnectionTimeToLive(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS)
                            .build(jsonConfig));

            logger.info(
                    "Configured OIDC HttpClient with connection TTL of {}s to prevent stale pooled connections",
                    CONNECTION_TTL_SECONDS);
        } catch (Exception e) {
            logger.error("Failed to configure OIDC connection TTL", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
