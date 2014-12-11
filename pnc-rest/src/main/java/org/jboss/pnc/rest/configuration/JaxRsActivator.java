package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.rest.endpoint.ProjectConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.TriggerBuildEndpoint;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);
        resources.add(TriggerBuildEndpoint.class);
        resources.add(ProjectConfigurationEndpoint.class);
        resources.add(IllegalArgumentExceptionMapper.class);
        return resources;
    }

}
