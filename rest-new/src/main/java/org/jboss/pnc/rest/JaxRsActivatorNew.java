/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest;

import org.jboss.pnc.rest.endpoints.ArtifactEndpointImpl;
import org.jboss.pnc.rest.endpoints.BuildConfigurationEndpointImpl;
import org.jboss.pnc.rest.endpoints.EnvironmentEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductMilestoneEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductReleaseEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductVersionEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProjectEndpointImpl;
import org.jboss.pnc.rest.endpoints.SCMRepositoryEndpointImpl;
import org.jboss.pnc.rest.endpoints.UserEndpointImpl;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

@ApplicationPath("/rest-new")
public class JaxRsActivatorNew extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public JaxRsActivatorNew() throws IOException {
        configureSwagger();
        configureCors();
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        addSwaggerResources(resources);
        addProjectResources(resources);
        addMetricsResources(resources);
        return resources;
    }

    private void configureCors () {
        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
        singletons.add(corsFilter);
    }

    private final void configureSwagger() throws IOException {
    }

    private void addProjectResources(Set<Class<?>> resources) {
        addEndpoints(resources);
        addExceptionMappers(resources);
    }

    private void addEndpoints(Set<Class<?>> resources) {
        resources.add(ArtifactEndpointImpl.class);

        resources.add(BuildConfigurationEndpointImpl.class);
        resources.add(ProjectEndpointImpl.class);

        resources.add(ProductEndpointImpl.class);
        resources.add(ProductMilestoneEndpointImpl.class);
        resources.add(ProductReleaseEndpointImpl.class);
        resources.add(ProductVersionEndpointImpl.class);

        resources.add(EnvironmentEndpointImpl.class);
        resources.add(SCMRepositoryEndpointImpl.class);
        resources.add(UserEndpointImpl.class);
    }

    private void addExceptionMappers(Set<Class<?>> resources) {
    }


    private void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(OpenApiResource.class);
    }

    private void addMetricsResources(Set<Class<?>> resources) {
    }


}
