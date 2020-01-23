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

package org.jboss.pnc.rest.endpoint;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.rest.provider.CacheProvider;

/**
 * Author: Andrea Vibelli, andrea.vibelli@gmail.com Date: 12/16/19 Time: 11:20 AM
 */
@Path("/cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheEndpoint {

    @Inject
    private CacheProvider cacheProvider;

    @Deprecated
    public CacheEndpoint() {
    }

    @Inject
    public CacheEndpoint(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @GET
    @Path("/statistics")
    @TimedMetric
    public Response getGenericStats() {
        return Response.ok(cacheProvider.getGenericStats()).build();
    }

    @GET
    @Path("/entity-statistics")
    public Response getSecondLevelCacheEntitiesStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheEntitiesStats()).build();
    }

    @GET
    @Path("/region-statistics")
    public Response getSecondLevelCacheRegionsStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheRegionsStats()).build();
    }

    @GET
    @Path("/entity-statistics")
    public Response getSecondLevelCacheCollectionsStats() {
        return Response.ok(cacheProvider.getSecondLevelCacheCollectionsStats()).build();
    }

    @DELETE
    public Response clearCache() {
        cacheProvider.clearAllCache();
        return Response.ok().build();
    }

}
