/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client;

import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClientBase<T> {

    // TODO: change it when the endpoint is updated
    protected final String BASE_PATH = "/pnc-rest-new/rest-new";

    protected final ResteasyClient client;

    protected final ResteasyWebTarget target;

    protected T proxy;

    protected Configuration configuration;

    protected ClientBase(Configuration configuration, Class<T> clazz) {
        this.configuration = configuration;
        client = new ResteasyClientBuilder()
                .httpEngine(new ApacheHttpClient43EngineWithRetry())
                .build();
        client.register(ResteasyJackson2ProviderWithDateISO8601.class);
        target = client.target(configuration.getProtocol() + "://" + configuration.getHost() + ":" + configuration.getPort() + BASE_PATH);
        Configuration.BasicAuth basicAuth = configuration.getBasicAuth();
        if (basicAuth != null) {
            target.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        }
        String bearerToken = configuration.getBearerToken();
        if (bearerToken != null && !bearerToken.equals("")) {
            target.register(new BearerAuthentication(bearerToken));
        }
        proxy = target.proxy(clazz);
    }

    protected T getEndpoint() {
        return proxy;
    }

    RemoteCollectionConfig getRemoteCollectionConfig() {
        Integer pageSize = configuration.getPageSize();
        if (pageSize == null || pageSize < 1) {
            pageSize = 100;
        }
        return new RemoteCollectionConfig(pageSize);
    }

    protected void setSortAndQuery(PageParameters pageParameters, Optional<String> sort, Optional<String> q) {
        sort.ifPresent(s -> pageParameters.setSort(s));
        q.ifPresent(query -> pageParameters.setQ(query));
    }

}
