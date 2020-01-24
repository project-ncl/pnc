package org.jboss.pnc.rest.endpoints.internal;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.pnc.facade.providers.api.CacheProvider;
import org.jboss.pnc.rest.api.endpoints.CacheEndpoint;

@Stateless
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
