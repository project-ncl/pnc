/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class AuthenticationProviderFactory {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationProviderFactory.class);

    private static final String DEFAULT_AUTHENTICATION_PROVIDER_ID = KeycloakAuthenticationProvider.ID;

    private AuthenticationProvider authenticationProvider;

    @Deprecated // CDI workaround
    public AuthenticationProviderFactory() {
    }

    @Inject
    public AuthenticationProviderFactory(
            @AuthProvider Instance<AuthenticationProvider> providers,
            Configuration configuration) throws CoreException {

        AtomicReference<String> providerId = new AtomicReference<>(null);
        try {
            providerId.set(
                    configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class))
                            .getAuthenticationProviderId());
        } catch (ConfigurationParseException e) {
            logger.warn("Unable parse config. Using default scheduler");
            providerId.set(DEFAULT_AUTHENTICATION_PROVIDER_ID);
        }
        providers.forEach(provider -> setMatchingProvider(provider, providerId.get()));
        if (authenticationProvider == null) {
            throw new CoreException(
                    "Cannot get AuthenticationProvider, check configurations and make sure a provider with configured id is available for injection. configured id: "
                            + providerId);
        }
    }

    private void setMatchingProvider(AuthenticationProvider provider, String id) {
        if (provider.getId().equals(id)) {
            logger.trace("Using {} as authentication provider.", id);
            authenticationProvider = provider;
        }
    }

    @Produces
    public AuthenticationProvider getProvider() {
        return authenticationProvider;
    }

}
