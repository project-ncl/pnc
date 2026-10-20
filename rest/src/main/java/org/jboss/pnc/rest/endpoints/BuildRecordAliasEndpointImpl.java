/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static org.jboss.pnc.facade.providers.api.UserRoles.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/build-records")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({ USERS, USERS_ADMIN })
public class BuildRecordAliasEndpointImpl {

    @GET
    @Path("/{id}")
    public Response getSpecific(@PathParam("id") String id, @Context UriInfo uriInfo) {
        // redirects to new Rest-api endpoint
        return Response.status(Response.Status.MOVED_PERMANENTLY)
                .location(uriInfo.getBaseUriBuilder().path("/builds").path("/" + id).build())
                .build();
    }
}
