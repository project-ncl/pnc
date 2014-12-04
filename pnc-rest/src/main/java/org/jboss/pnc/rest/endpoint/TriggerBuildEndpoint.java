package org.jboss.pnc.rest.endpoint;

import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.rest.trigger.BuildTriggerer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;

@Path("/configuration")
public class TriggerBuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildTriggerer buildTriggerer;

    public TriggerBuildEndpoint() {
    }

    @Inject
    public TriggerBuildEndpoint(BuildTriggerer buildTriggerer) {
        this.buildTriggerer = buildTriggerer;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/build")
    public Response getSpecificBuild(@PathParam("id") Integer id) {
        try {
            buildTriggerer.triggerBuilds(id);
            return Response.ok().build();
        } catch (CoreException e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Core error: " + e.getMessage()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.serverError().entity("Other error: " + e.getMessage()).build();
        }
    }
}
