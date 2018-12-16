/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client;

import io.swagger.v3.oas.annotations.Parameter;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.processor.annotation.ClientApi;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.jboss.pnc.rest.api.endpoints.BuildConfigurationEndpoint.BC_ID;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ClientApi
public interface ProjectEndpointX {
    static final String P_ID = "ID of the project";

    @GET
    Page<Project> getAll(@BeanParam PageParameters pageParameters);

    @GET
    @Path("/{id}")
    public Project getSpecific(@Parameter(description = P_ID) @PathParam("id") String id);

    @GET
    @Path("/{id}/build-configurations")
    public Page<BuildConfiguration> getBuildConfigurations(
            @Parameter(description = "Project Id") @PathParam("id") String id,
            @BeanParam PageParameters pageParameters);


    @GET
    @Path("/{id}/builds")
    Page<Build> getBuilds(
            @Parameter(description = BC_ID) @PathParam("id") String id,
            @BeanParam PageParameters pageParams,
            @BeanParam BuildsFilterParameters buildsFilter);

}
