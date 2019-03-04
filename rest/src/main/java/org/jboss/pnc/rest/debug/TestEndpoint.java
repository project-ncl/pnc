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
package org.jboss.pnc.rest.debug;

import io.swagger.v3.oas.annotations.Hidden;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Hidden
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TestEndpoint {

    @Inject
    Event<BuildCoordinationStatusChangedEvent> buildStatusChangedEventEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventEvent;

    @POST
    @Path("/build-status/notify")
    public void sendBuildStatusChangedEvent(@Valid BuildStatusChangedEventRest buildStatusChangedEventRest) {
        buildStatusChangedEventEvent.fire(buildStatusChangedEventRest);
    }

    @POST
    @Path("/build-set-status/notify")
    public void sendBuildSetStatusChangedEvent(@Valid BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        buildSetStatusChangedEventEvent.fire(buildSetStatusChangedEvent);
    }

    @GET
    @Path("/throw")
    public void throwEx() throws Exception {
        RuntimeException nested = new RuntimeException("Root exception.");
        throw new Exception("Test exception.", nested);
    }

    @GET
    @Path("/unauthorized")
    public void redirect() throws Exception {
        Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer realm=\"test\"")
                .build();
    }

    @GET
    @Path("/nocontent")
    public void nocontent() throws Exception {
        Response.status(Response.Status.NO_CONTENT).build();
    }
}
