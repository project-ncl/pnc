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

import io.swagger.v3.oas.annotations.Hidden;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Optional;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.QUERY_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SORTING_QUERY_PARAM;

/**
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Hidden
@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtifactEndpoint extends AbstractEndpoint<Artifact,ArtifactRest> {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpoint.class);

    private ArtifactProvider artifactProvider;

    public ArtifactEndpoint() {
    }

    @Inject
    public ArtifactEndpoint(ArtifactProvider artifactProvider) {
        super(artifactProvider);
        this.artifactProvider = artifactProvider;
    }

    @GET
    public Response getAll(@QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize,
            @QueryParam(SORTING_QUERY_PARAM) String sort,
            @QueryParam(QUERY_QUERY_PARAM) String q,
            @QueryParam("sha256") String sha256,
            @QueryParam("md5") String md5,
            @QueryParam("sha1") String sha1) {
        return fromCollection(artifactProvider.getAll(pageIndex, pageSize, sort, q, Optional.ofNullable(sha256),
                Optional.ofNullable(md5), Optional.ofNullable(sha1)));
    }

    @GET
    @Path("/{id}")
    public Response getSpecific(
            @PathParam("id") Integer id) {
        return super.getSpecific(id);
    }
}
