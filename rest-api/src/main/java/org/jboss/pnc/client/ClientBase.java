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

import org.jboss.pnc.client.patch.PatchBase;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClientBase<T> {

    private Logger logger = LoggerFactory.getLogger(ClientBase.class);

    // TODO: change it when the endpoint is updated
    protected final String BASE_PATH = "/pnc-rest-new/rest-new";

    protected final ResteasyClient client;

    protected final ResteasyWebTarget target;

    protected T proxy;

    protected Configuration configuration;

    protected Class<T> iface;

    protected ClientBase(Configuration configuration, Class<T> iface) {
        this.iface = iface;

        ApacheHttpClient43EngineWithRetry engine = new ApacheHttpClient43EngineWithRetry();
        // allow redirects for NCL-3766
        engine.setFollowRedirects(true);

        this.configuration = configuration;

        client = ((ResteasyClientBuilder)ClientBuilder.newBuilder())
                .httpEngine(engine)
                .build();
        client.register(ResteasyJackson2ProviderWithDateISO8601.class);
        client.register(new MdcToHeadersFilter(configuration.getMdcToHeadersMappings()));
        client.register(RequestLoggingFilter.class);
        target = client.target(configuration.getProtocol() + "://" + configuration.getHost() + ":" + configuration.getPort() + BASE_PATH);
        Configuration.BasicAuth basicAuth = configuration.getBasicAuth();
        if (basicAuth != null) {
            target.register(new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword()));
        }
        String bearerToken = configuration.getBearerToken();
        if (bearerToken != null && !bearerToken.equals("")) {
            target.register(new BearerAuthentication(bearerToken));
        }
        proxy = target.proxy(iface);
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

    public <S> S patch(String id, String jsonPatch, Class<S> clazz) {
        Path path = iface.getAnnotation(Path.class);
        ResteasyWebTarget patchTarget;
        if (!path.value().equals("") && !path.value().equals("/")) {
            patchTarget = target.path(path.value() + "/" + id);
        } else {
            patchTarget = target.path(id);
        }

        logger.debug("Json patch: {}", jsonPatch);

        S result = patchTarget.request()
                .build(HttpMethod.PATCH, Entity.entity(jsonPatch, MediaType.APPLICATION_JSON_PATCH_JSON))
                .invoke(clazz);

        return result;
    }

    public InputStream getInputStream(String methodPath, String id) {
        Path path = iface.getAnnotation(Path.class);

        String interfacePath = path.value();

        ResteasyWebTarget webTarget = target.path(interfacePath + methodPath).resolveTemplate("id", id);

        return webTarget.request()
                .build(HttpMethod.GET)
                .invoke(InputStream.class);
    }

    public <S> S patch(String id, PatchBase patchBase) throws PatchBuilderException {
        String jsonPatch = patchBase.getJsonPatch();
        return patch(id, jsonPatch, (Class<S>)patchBase.getClazz());
    }

    protected ErrorResponse readErrorResponse(WebApplicationException ex){
        Response response = ex.getResponse();
        if(response.hasEntity()){
            try{
                return response.readEntity(ErrorResponse.class);
            }catch(ProcessingException | IllegalStateException e){
                logger.debug("Can't map response to ErrorResponse.", e);
            }catch(RuntimeException e){
                logger.warn("Unexpected exception when trying to read ErrorResponse.", e);
            }
        }
        return null;
    }
}
