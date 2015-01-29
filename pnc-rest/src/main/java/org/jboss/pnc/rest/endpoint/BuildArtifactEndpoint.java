package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.BuildArtifactProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Api(value = "/result/{buildRecordId}/artifact", description = "Results of building process")
@Path("/result/{buildRecordId}/artifact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildArtifactEndpoint {

    private BuildArtifactProvider buildArtifactProvider;

    public BuildArtifactEndpoint() {
    }

    @Inject
    public BuildArtifactEndpoint(BuildArtifactProvider buildArtifactProvider) {
        this.buildArtifactProvider = buildArtifactProvider;
    }

    @ApiOperation(value = "Gets all Build Artifacts")
    @GET
    public List<ArtifactRest> getAll(
            @ApiParam(value = "Build Result id", required = true) @PathParam("buildRecordId") Integer buildRecordId) {
        return buildArtifactProvider.getAll(buildRecordId);
    }
}
