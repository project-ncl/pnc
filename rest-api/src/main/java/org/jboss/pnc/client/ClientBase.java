/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.client.patch.PatchBase;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
public abstract class ClientBase<T> {

    private Logger logger = LoggerFactory.getLogger(ClientBase.class);

    protected final String BASE_PATH = "/pnc-rest";

    // TODO: change it when the endpoint is updated
    protected final String BASE_REST_PATH = BASE_PATH + "/rest-new";

    protected final Client client;

    protected final WebTarget target;

    protected T proxy;

    protected Configuration configuration;

    protected Class<T> iface;

    protected BearerAuthentication bearerAuthentication;

    protected ClientBase(Configuration configuration, Class<T> iface) {
        this.iface = iface;

        ApacheHttpClient43EngineWithRetry engine = new ApacheHttpClient43EngineWithRetry();
        // allow redirects for NCL-3766
        engine.setFollowRedirects(true);

        this.configuration = configuration;

        ResteasyClientBuilder clientBuilder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
        client = clientBuilder.httpEngine(engine).build();
        client.register(ResteasyJackson2ProviderWithDateISO8601.class);
        client.register(new MdcToHeadersFilter(configuration.getMdcToHeadersMappings()));
        client.register(RequestLoggingFilter.class);
        target = client.target(
                configuration.getProtocol() + "://" + configuration.getHost() + ":" + configuration.getPort()
                        + BASE_REST_PATH);
        Configuration.BasicAuth basicAuth = configuration.getBasicAuth();

        if (basicAuth != null) {
            target.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        } else {
            if (configuration.getBearerTokenSupplier() != null) {
                bearerAuthentication = new BearerAuthentication(configuration.getBearerTokenSupplier().get());
                target.register(bearerAuthentication);
            } else {
                String bearerToken = configuration.getBearerToken();
                if (bearerToken != null && !bearerToken.equals("")) {
                    bearerAuthentication = new BearerAuthentication(bearerToken);
                    target.register(bearerAuthentication);
                }
            }
        }

        proxy = ProxyBuilder.builder(iface, target).build();
    }

    protected T getEndpoint() {
        return proxy;
    }

    RemoteCollectionConfig getRemoteCollectionConfig() {
        int pageSize = configuration.getPageSize();
        if (pageSize < 1) {
            pageSize = 100;
        }
        return new RemoteCollectionConfig(pageSize);
    }

    protected void setSortAndQuery(PageParameters pageParameters, Optional<String> sort, Optional<String> q) {
        sort.ifPresent(s -> pageParameters.setSort(s));
        q.ifPresent(query -> pageParameters.setQ(query));
    }

    public <S> S patch(String id, String jsonPatch, Class<S> clazz) throws RemoteResourceException {
        Path path = iface.getAnnotation(Path.class);
        WebTarget patchTarget;
        if (!path.value().equals("") && !path.value().equals("/")) {
            patchTarget = target.path(path.value() + "/" + id);
        } else {
            patchTarget = target.path(id);
        }

        logger.debug("Json patch: {}", jsonPatch);

        try {
            S result = patchTarget.request()
                    .build(HttpMethod.PATCH, Entity.entity(jsonPatch, MediaType.APPLICATION_JSON_PATCH_JSON))
                    .invoke(clazz);
            return result;
        } catch (WebApplicationException e) {
            throw new RemoteResourceException(readErrorResponse(e), e);
        }
    }

    public InputStream getInputStream(String methodPath, String id) {
        Path path = iface.getAnnotation(Path.class);

        String interfacePath = path.value();

        WebTarget webTarget = target.path(interfacePath + methodPath).resolveTemplate("id", id);

        return webTarget.request().build(HttpMethod.GET).invoke(InputStream.class);
    }

    public <S> S patch(String id, PatchBase patchBase) throws PatchBuilderException, RemoteResourceException {
        String jsonPatch = patchBase.getJsonPatch();
        try {
            return patch(id, jsonPatch, (Class<S>) patchBase.getClazz());
        } catch (WebApplicationException e) {
            throw new RemoteResourceException(readErrorResponse(e), e);
        }
    }

    protected ErrorResponse readErrorResponse(WebApplicationException ex) {
        Response response = ex.getResponse();
        if (response.hasEntity()) {
            try {
                return response.readEntity(ErrorResponse.class);
            } catch (ProcessingException | IllegalStateException e) {
                logger.debug("Can't map response to ErrorResponse.", e);
            } catch (RuntimeException e) {
                logger.warn("Unexpected exception when trying to read ErrorResponse.", e);
            }
        }
        return null;
    }
}
