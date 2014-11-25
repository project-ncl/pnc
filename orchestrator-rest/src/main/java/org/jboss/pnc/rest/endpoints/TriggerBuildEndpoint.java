package org.jboss.pnc.rest.endpoints;


import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.rest.trigger.RestBuildTrigger;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/trigger")
@RequestScoped
public class TriggerBuildEndpoint {

    RestBuildTrigger trigger;

    @POST
    public BuildInfo triggerBuild(BuildIdentifier buildId) {
        return trigger.startBuild(buildId);
    }

}
