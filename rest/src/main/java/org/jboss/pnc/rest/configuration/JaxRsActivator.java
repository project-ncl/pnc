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
package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.rest.debug.TestEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigSetRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordSetEndpoint;
import org.jboss.pnc.rest.endpoint.EnvironmentEndpoint;
import org.jboss.pnc.rest.endpoint.LicenseEndpoint;
import org.jboss.pnc.rest.endpoint.ProductEndpoint;
import org.jboss.pnc.rest.endpoint.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.endpoint.ProductReleaseEndpoint;
import org.jboss.pnc.rest.endpoint.ProductVersionEndpoint;
import org.jboss.pnc.rest.endpoint.ProjectEndpoint;
import org.jboss.pnc.rest.endpoint.RunningBuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.UserEndpoint;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        addSwaggerResources(resources);
        addProjectResources(resources);
        return resources;
    }

    private void addProjectResources(Set<Class<?>> resources) {
        resources.add(ProductEndpoint.class);
        resources.add(ProductVersionEndpoint.class);
        resources.add(ProductMilestoneEndpoint.class);
        resources.add(ProductReleaseEndpoint.class);
        resources.add(ProjectEndpoint.class);
        resources.add(BuildConfigurationEndpoint.class);
        resources.add(BuildConfigurationSetEndpoint.class);
        resources.add(BuildConfigSetRecordEndpoint.class);
        resources.add(BuildRecordEndpoint.class);
        resources.add(BuildRecordSetEndpoint.class);
        resources.add(RunningBuildRecordEndpoint.class);
        resources.add(UserEndpoint.class);
        resources.add(LicenseEndpoint.class);
        resources.add(EnvironmentEndpoint.class);
        resources.add(TestEndpoint.class);
        resources.add(ValidationExceptionExceptionMapper.class);
        resources.add(AllOtherExceptionsMapper.class);
    }

    private void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
    }

}
