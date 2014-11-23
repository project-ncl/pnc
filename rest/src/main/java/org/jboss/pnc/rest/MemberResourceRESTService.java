package org.jboss.pnc.rest;

import org.jboss.pnc.model.Project;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

/**
 * JAX-RS Example
 * 
 * This class produces a RESTful service to read the contents of the members table.
 */
@Path("/members")
@RequestScoped
public class MemberResourceRESTService {

    @GET
    public List<Project> listAllMembers() {
        return null; //TODO
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    public Project lookupMemberById(@PathParam("id") long id) {
        return null; //TODO
    }
}
