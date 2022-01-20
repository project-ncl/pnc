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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.inject.Produces;

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
