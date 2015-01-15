package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.rest.endpoint.*;

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
        resources.add(TriggerBuildEndpoint.class);
        resources.add(ProductEndpoint.class);
        resources.add(ProductVersionEndpoint.class);
        resources.add(ProjectEndpoint.class);
        resources.add(BuildConfigurationEndpoint.class);
        resources.add(LegacyEndpoint.class);
        resources.add(BuildRecordEndpoint.class);
        resources.add(RunningBuildRecordEndpoint.class);
        resources.add(UserEndpoint.class);
        resources.add(IllegalArgumentExceptionMapper.class);
    }

    private void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);
    }

}
