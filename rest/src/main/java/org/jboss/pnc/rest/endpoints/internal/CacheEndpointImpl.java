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
package org.jboss.pnc.rest.endpoints.internal;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.pnc.facade.providers.api.CacheProvider;
import org.jboss.pnc.rest.api.endpoints.CacheEndpoint;

@ApplicationScoped
public class CacheEndpointImpl implements CacheEndpoint {

    @Inject
    private CacheProvider cacheProvider;

    @Override
    public Response getGenericStats() {
        return Response.ok(cacheProvider.getGenericStats()).build();
    }

    @Override
    public Response getSecondLevelCacheEntitiesStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheEntitiesStats()).build();
    }

    @Override
    public Response getSecondLevelCacheRegionsStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheRegionsStats()).build();
    }

    @Override
    public Response getSecondLevelCacheCollectionsStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheCollectionsStats()).build();
    }

    @Override
    public Response clearCache() {
        cacheProvider.clearAllCache();
        return Response.ok().build();
    }

}
