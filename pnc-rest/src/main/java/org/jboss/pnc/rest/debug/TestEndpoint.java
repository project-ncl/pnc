package org.jboss.pnc.rest.debug;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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

    @POST
    @Path("/buildstatus/notify")
    @ApiOperation(value = "Sends BuildStatusChangedEvent just like it was from Core, useful for testing WebSockets")
    public void sendBuildStatusChangedEvent(BuildStatusChangedEventRest buildStatusChangedEventRest) {
        buildStatusChangedEventEvent.fire(buildStatusChangedEventRest);
    }
}
