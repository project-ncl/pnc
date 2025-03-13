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
package org.jboss.pnc.coordinator.maintenance;

import org.apache.commons.lang.StringUtils;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.IndyClientHttp;
import org.commonjava.indy.client.core.IndyClientModule;
import org.commonjava.indy.client.core.auth.IndyClientAuthenticator;
import org.commonjava.indy.client.core.auth.OAuth20BearerTokenAuthenticator;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.commonjava.indy.promote.client.IndyPromoteClientModule;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.logging.MDCUtils;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class IndyFactory {

    private Integer defaultRequestTimeout;
    private String baseUrl;
    private Indy indy;

    @Deprecated // CDI workaround
    public IndyFactory() {
    }

    @PreDestroy
    void clean() {
        if (indy != null) {
            indy.close();
        }
    }

    @Inject
    public IndyFactory(GlobalModuleGroup globalConfig, IndyRepoDriverModuleConfig config) {
        this.defaultRequestTimeout = config.getDefaultRequestTimeout();

        String baseUrl = StringUtils.stripEnd(globalConfig.getIndyUrl(), "/");
        if (!baseUrl.endsWith("/api")) {
            baseUrl += "/api";
        }
        this.baseUrl = baseUrl;
    }

    @Produces
    public Indy get(String accessToken) {
        if (indy != null) {
            return indy;
        }

        IndyClientAuthenticator authenticator = null;
        if (accessToken != null) {
            authenticator = new OAuth20BearerTokenAuthenticator(accessToken);
        }

        try {
            SiteConfig siteConfig = new SiteConfigBuilder("indy", baseUrl)
                    .withRequestTimeoutSeconds(defaultRequestTimeout)
                    .withMaxConnections(IndyClientHttp.GLOBAL_MAX_CONNECTIONS)
                    .build();

            IndyClientModule[] modules = new IndyClientModule[] { new IndyFoloAdminClientModule(),
                    new IndyPromoteClientModule() };
            this.indy = new Indy(
                    siteConfig,
                    authenticator,
                    new IndyObjectMapper(true),
                    MDCUtils.HEADER_KEY_MAPPING,
                    modules);
            return this.indy;
        } catch (IndyClientException e) {
            throw new IllegalStateException("Failed to create Indy client: " + e.getMessage(), e);
        }
    }

}
