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
package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.pncmetrics.rest.GeneralRestMetricsFilter;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.pncmetrics.rest.TimedMetricFilter;
import org.jboss.pnc.rest.debug.TestEndpoint;
import org.jboss.pnc.rest.endpoint.ArtifactEndpoint;
import org.jboss.pnc.rest.endpoint.BpmEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigSetRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.endpoint.BuildEndpoint;
import org.jboss.pnc.rest.endpoint.BuildEnvironmentEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordPushEndpoint;
import org.jboss.pnc.rest.endpoint.BuildTaskEndpoint;
import org.jboss.pnc.rest.endpoint.DebugEndpoint;
import org.jboss.pnc.rest.endpoint.ProductEndpoint;
import org.jboss.pnc.rest.endpoint.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.endpoint.ProductReleaseEndpoint;
import org.jboss.pnc.rest.endpoint.ProductVersionEndpoint;
import org.jboss.pnc.rest.endpoint.ProjectEndpoint;
import org.jboss.pnc.rest.endpoint.RepositoryConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.RunningBuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.UserEndpoint;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public JaxRsActivator() throws IOException {
        configureCors();
        singletons.add(new MDCLoggingFilter());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
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

    private void addProjectResources(Set<Class<?>> resources) {
        addEndpoints(resources);
        addExceptionMappers(resources);
    }

    private void addEndpoints(Set<Class<?>> resources) {
        resources.add(ArtifactEndpoint.class);
        resources.add(ProductEndpoint.class);
        resources.add(ProductVersionEndpoint.class);
        resources.add(ProductMilestoneEndpoint.class);
        resources.add(ProductReleaseEndpoint.class);
        resources.add(ProjectEndpoint.class);
        resources.add(RepositoryConfigurationEndpoint.class);
        resources.add(BuildConfigurationEndpoint.class);
        resources.add(BuildConfigurationSetEndpoint.class);
        resources.add(BuildConfigSetRecordEndpoint.class);
        resources.add(BuildRecordEndpoint.class);
        resources.add(BuildRecordPushEndpoint.class);
        resources.add(RunningBuildRecordEndpoint.class);
        resources.add(UserEndpoint.class);
        resources.add(BuildEnvironmentEndpoint.class);
        resources.add(TestEndpoint.class);
        resources.add(BuildTaskEndpoint.class);
        resources.add(BuildEndpoint.class);
        resources.add(BpmEndpoint.class);
        resources.add(DebugEndpoint.class);
    }

    private void addExceptionMappers(Set<Class<?>> resources) {
        resources.add(ValidationExceptionExceptionMapper.class);
        resources.add(BuildConflictExceptionMapper.class);
        resources.add(AllOtherExceptionsMapper.class);
    }

    private void addMetricsResources(Set<Class<?>> resources) {
        resources.add(GeneralRestMetricsFilter.class);
        resources.add(TimedMetric.class);
        resources.add(TimedMetricFilter.class);
    }

    private String getRestBasePath() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("/swagger.properties");
        Properties properties = new Properties();
        try (InputStream inStream = resource.openStream()) {
            properties.load(inStream);
        }
        return properties.getProperty("baseUrl");
    }

}
