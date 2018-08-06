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
package org.jboss.pnc.rest.endpoint;

import io.swagger.annotations.Api;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.api.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.configuration.BuildConfigurationSupportedGenericParameters;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.spi.BuildOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.lang.invoke.MethodHandles;

import org.jboss.pnc.rest.model.BuildConfigurationRest;
import org.jboss.pnc.rest.model.response.Singleton;
import org.jboss.pnc.rest.provider.api.BuildConfigurationProvider;

@Api(value = "/build-configurations", description = "Build configuration entities")
@Path("/build-configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuildConfigurationEndpointImpl extends AbstractEndpoint<BuildConfiguration, BuildConfigurationRest> implements BuildConfigurationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigurationProvider buildConfigurationProvider;

    private java.util.Map<String, String> buildConfigurationSupportedGenericParameters;

    public static void checkBuildOptionsValidity(BuildOptions buildOptions) throws InvalidEntityException {
        if (!buildOptions.isTemporaryBuild() && buildOptions.isTimestampAlignment()) {
            // Combination timestampAlignment + standard build is not allowed
            throw new InvalidEntityException("Combination of the build parameters is not allowed. Timestamp alignment is allowed only for temporary builds. ");
        }
    }

    public BuildConfigurationEndpointImpl() {
    }

    @Inject
    public BuildConfigurationEndpointImpl(
            BuildConfigurationProvider buildConfigurationProvider,
            BuildConfigurationSupportedGenericParameters supportedGenericParameters) {

        super(buildConfigurationProvider);
        this.buildConfigurationProvider = buildConfigurationProvider;

        this.buildConfigurationSupportedGenericParameters = supportedGenericParameters
                .getSupportedGenericParameters();
    }

    @Override
    public Response getAll(int pageIndex, int pageSize, String sort, String q) {
        return fromCollection(buildConfigurationProvider.getAllNonArchived(pageIndex, pageSize, sort, q));
    }

    @Override
    public Response createNew(BuildConfigurationRest buildConfigurationRest, @Context UriInfo uriInfo) {
        return super.createNew(buildConfigurationRest, uriInfo);
    }

    @Override
    public Response getSupportedGenericParameters() {
        return Response.ok().entity(buildConfigurationSupportedGenericParameters).build();
    }

    @Override
    public Response getSpecific(Integer id) {
        return super.getSpecific(id);
    }

    @Override
    public Response update(Integer id, BuildConfigurationRest buildConfigurationRest) {
        return super.update(id, buildConfigurationRest);
    }

    @Override
    public Response deleteSpecific(Integer id) {
        buildConfigurationProvider.archive(id);
        return Response.ok().build();
    }

    @Override
    public Response clone(Integer id, @Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path("/build-configurations/{id}");
        int newId = buildConfigurationProvider.clone(id);
        return Response.created(uriBuilder.build(newId)).entity(new Singleton(buildConfigurationProvider.getSpecific(newId))).build();
    }

    @Override
    public Response getAllByProjectId(int pageIndex, int pageSize, String sort, String q, Integer projectId) {
        return fromCollection(buildConfigurationProvider.getAllForProject(pageIndex, pageSize, sort, q, projectId));
    }

    @Override
    public Response getAllByProductId(int pageIndex, int pageSize, String sort, String q, Integer productId) {
        return fromCollection(buildConfigurationProvider.getAllForProduct(pageIndex, pageSize, sort, q, productId));
    }

    @Override
    public Response getAllByProductVersionId(int pageIndex, int pageSize, String sort, String q, Integer productId, Integer versionId) {
        return fromCollection(buildConfigurationProvider
                .getAllForProductAndProductVersion(pageIndex, pageSize, sort, q, productId, versionId));
    }

    @Override
    public Response getDependencies(int pageIndex, int pageSize, String sort, String q, Integer id) {
        return fromCollection(buildConfigurationProvider.getDependencies(pageIndex, pageSize, sort, q, id));
    }

    @Override
    public Response addDependency(Integer id, BuildConfigurationRest dependency) {
        buildConfigurationProvider.addDependency(id, dependency.getId());
        return fromEmpty();
    }

    @Override
    public Response removeDependency(Integer id, Integer dependencyId) {
        buildConfigurationProvider.removeDependency(id, dependencyId);
        return fromEmpty();
    }
}
