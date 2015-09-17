/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "/test", description = "This is special Endpoint for testing purpose. Use with caution.")
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TestEndpoint {

    @Inject
    Event<BuildStatusChangedEvent> buildStatusChangedEventEvent;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventEvent;

    @POST
    @Path("/build-status/notify")
    @ApiOperation(value = "Sends BuildStatusChangedEvent just like it was from Core, useful for testing WebSockets")
    public void sendBuildStatusChangedEvent(@Valid BuildStatusChangedEventRest buildStatusChangedEventRest) {
        buildStatusChangedEventEvent.fire(buildStatusChangedEventRest);
    }

    @POST
    @Path("/build-set-status/notify")
    @ApiOperation(value = "Sends BuildSetStatusChangedEvent just like it was from Core, useful for testing WebSockets")
    public void sendBuildSetStatusChangedEvent(@Valid BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        buildSetStatusChangedEventEvent.fire(buildSetStatusChangedEvent);
    }
}
