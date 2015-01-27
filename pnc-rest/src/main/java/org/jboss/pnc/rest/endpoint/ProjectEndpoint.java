package org.jboss.pnc.rest.endpoint;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "/project", description = "Project related information")
@Path("/project")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectEndpoint {

    private ProjectProvider projectProvider;

    public ProjectEndpoint() {
    }

    @Inject
    public ProjectEndpoint(ProjectProvider projectProvider) {
        this.projectProvider = projectProvider;
    }

    @ApiOperation(value = "Gets all Projects")
    @GET
    public List<ProjectRest> getAll() {
        return projectProvider.getAll();
    }

    @ApiOperation(value = "Gets specific Project")
    @GET
    @Path("/{id}")
    public ProjectRest getSpecific(@ApiParam(value = "Project id", required = true) @PathParam("id") Integer id) {
        return projectProvider.getSpecific(id);
    }
}
