package org.jboss.pnc.rest.endpoints;


import org.jboss.pnc.core.buildinfo.BuildInfoRepository;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;

@Path("/builds")
@RequestScoped
public class RunningBuildsEndpoint {

    BuildInfoRepository buildInfoRepository;

    @GET
    public List<BuildInfo> getAllRunningBuilds() {
        return buildInfoRepository.findRunningBuilds();
    }

}
