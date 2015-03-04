package org.jboss.pnc.rest.endpoint;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Api(value = "/result/{buildRecordId}/artifact", description = "Results of building process")
@Path("/result/{buildRecordId}/artifact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtifactEndpoint {

    private ArtifactProvider artifactProvider;

    public ArtifactEndpoint() {
    }

    @Inject
    public ArtifactEndpoint(ArtifactProvider artifactProvider) {
        this.artifactProvider = artifactProvider;
    }

    @ApiOperation(value = "Gets all Build Artifacts")
    @GET
    public List<ArtifactRest> getAll(
            @ApiParam(value = "Page index") @QueryParam("pageIndex") @DefaultValue("0") int pageIndex,
            @ApiParam(value = "Pagination size") @DefaultValue("50") @QueryParam("pageSize") int pageSize,
            @ApiParam(value = "Sorting RSQL") @QueryParam("sort") String sortingRsql,
            @ApiParam(value = "RSQL query", required = false) @QueryParam("q") String rsql,
            @ApiParam(value = "Build Result id", required = true) @PathParam("buildRecordId") Integer buildRecordId) {
        return artifactProvider.getAll(pageIndex, pageSize, sortingRsql, rsql, buildRecordId);
    }
}
