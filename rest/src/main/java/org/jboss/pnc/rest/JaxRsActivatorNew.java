/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.jboss.pnc.common.json.moduleconfig.KeycloakClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.pncmetrics.rest.GeneralRestMetricsFilter;
import org.jboss.pnc.pncmetrics.rest.TimedMetric;
import org.jboss.pnc.pncmetrics.rest.TimedMetricFilter;
import org.jboss.pnc.rest.endpoints.ArtifactEndpointImpl;
import org.jboss.pnc.rest.endpoints.BuildConfigurationEndpointImpl;
import org.jboss.pnc.rest.endpoints.BuildEndpointImpl;
import org.jboss.pnc.rest.endpoints.BuildRecordAliasEndpointImpl;
import org.jboss.pnc.rest.endpoints.EnvironmentEndpointImpl;
import org.jboss.pnc.rest.endpoints.GroupBuildEndpointImpl;
import org.jboss.pnc.rest.endpoints.GroupConfigurationEndpointImpl;
import org.jboss.pnc.rest.endpoints.OperationEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductMilestoneEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductReleaseEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProductVersionEndpointImpl;
import org.jboss.pnc.rest.endpoints.ProjectEndpointImpl;
import org.jboss.pnc.rest.endpoints.SCMRepositoryEndpointImpl;
import org.jboss.pnc.rest.endpoints.TargetRepositoryEndpointImpl;
import org.jboss.pnc.rest.endpoints.UserEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.BpmEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.BuildExecutionEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.BuildMaintenanceEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.BuildTaskEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.CacheEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.DebugEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.DeliverableAnalysisEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.GenericSettingEndpointImpl;
import org.jboss.pnc.rest.endpoints.internal.HealthCheckEndpointImpl;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.rest.provider.AllOtherExceptionsMapper;
import org.jboss.pnc.rest.provider.AlreadyRunningExceptionsMapper;
import org.jboss.pnc.rest.provider.BpmExceptionMapper;
import org.jboss.pnc.rest.provider.BuildConflictExceptionMapper;
import org.jboss.pnc.rest.provider.ConstraintViolationExceptionMapper;
import org.jboss.pnc.rest.provider.EJBExceptionMapper;
import org.jboss.pnc.rest.provider.OperationNotAllowedExceptionsMapper;
import org.jboss.pnc.rest.provider.RSQLExceptionMapper;
import org.jboss.pnc.rest.provider.RespondWithStatusFilter;
import org.jboss.pnc.rest.provider.UnauthorizedExceptionMapper;
import org.jboss.pnc.rest.provider.ValidationExceptionExceptionMapper;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/v2")
@ApplicationScoped
public class JaxRsActivatorNew extends Application {
    private static final Logger logger = LoggerFactory.getLogger(JaxRsActivatorNew.class);

    private static final String KEYCLOAK_AUTH = "keycloakAuth";

    @Inject
    private SystemConfig systemConfig;

    @Context
    private ServletConfig servletConfig;

    private Set<Object> singletons = new HashSet<>();

    public JaxRsActivatorNew() throws IOException {
        configureCors();
    }

    @PostConstruct
    public void init() {
        configureSwagger();
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
        addRespondWithStatusFilter(resources);
        addProviders(resources);
        resources.add(RequestLoggingFilter.class);
        return resources;
    }

    private void configureCors() {
        CorsFilter corsFilter = new CorsFilter();
        corsFilter.getAllowedOrigins().add("*");
        corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
        singletons.add(corsFilter);
    }

    private void configureSwagger() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info().title("PNC")
                .description("PNC build system")
                .termsOfService("http://swagger.io/terms/")
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        oas.info(info);
        oas.addServersItem(new Server().url("/pnc-rest"));

        final SecurityScheme authScheme = getAuthScheme();
        if (authScheme == null) {
            logger.warn("Not adding auth scheme to openapi definition as auth scheme could not been generated.");
        } else {
            oas.schemaRequirement(KEYCLOAK_AUTH, authScheme);
            oas.addSecurityItem(new SecurityRequirement().addList(KEYCLOAK_AUTH));
        }

        SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas);

        try {
            new JaxrsOpenApiContextBuilder().servletConfig(servletConfig)
                    .application(this)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException ex) {
            throw new IllegalArgumentException("Failed to setup OpenAPI configuration", ex);
        }
    }

    private SecurityScheme getAuthScheme() {
        try {
            final KeycloakClientConfig keycloakConfig = systemConfig.getKeycloakServiceAccountConfig();
            if (keycloakConfig == null || StringUtils.isEmpty(keycloakConfig.getAuthServerUrl())) {
                return null;
            }
            URI keycloakURL = new URI(keycloakConfig.getAuthServerUrl() + "/")
                    .resolve("realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/auth");

            final OAuthFlow implicitFlow = new OAuthFlow().authorizationUrl(keycloakURL.toString());

            SecurityScheme scheme = new SecurityScheme();
            scheme.type(SecurityScheme.Type.OAUTH2)
                    .description("This application uses Keycloak oauth authentication")
                    .flows(new OAuthFlows().implicit(implicitFlow));
            return scheme;
        } catch (URISyntaxException ex) {
            logger.warn("Failed to parse Keycloak setting", ex);
            return null;
        }
    }

    private void addProjectResources(Set<Class<?>> resources) {
        addEndpoints(resources);
        addExceptionMappers(resources);
    }

    private void addEndpoints(Set<Class<?>> resources) {
        resources.add(ArtifactEndpointImpl.class);

        resources.add(BpmEndpointImpl.class);
        resources.add(BuildMaintenanceEndpointImpl.class);
        resources.add(BuildEndpointImpl.class);
        resources.add(BuildExecutionEndpointImpl.class);
        resources.add(BuildTaskEndpointImpl.class);

        resources.add(BuildConfigurationEndpointImpl.class);
        resources.add(GroupBuildEndpointImpl.class);
        resources.add(GroupConfigurationEndpointImpl.class);
        resources.add(ProjectEndpointImpl.class);

        resources.add(ProductEndpointImpl.class);
        resources.add(ProductMilestoneEndpointImpl.class);
        resources.add(ProductReleaseEndpointImpl.class);
        resources.add(ProductVersionEndpointImpl.class);

        resources.add(EnvironmentEndpointImpl.class);
        resources.add(SCMRepositoryEndpointImpl.class);
        resources.add(TargetRepositoryEndpointImpl.class);
        resources.add(UserEndpointImpl.class);

        resources.add(BuildRecordAliasEndpointImpl.class);

        resources.add(HealthCheckEndpointImpl.class);
        resources.add(GenericSettingEndpointImpl.class);
        resources.add(CacheEndpointImpl.class);
        resources.add(DebugEndpointImpl.class);
        resources.add(DeliverableAnalysisEndpointImpl.class);
        resources.add(OperationEndpointImpl.class);
    }

    private void addExceptionMappers(Set<Class<?>> resources) {
        resources.add(AllOtherExceptionsMapper.class);
        resources.add(AlreadyRunningExceptionsMapper.class);
        resources.add(BpmExceptionMapper.class);
        resources.add(BuildConflictExceptionMapper.class);
        resources.add(ConstraintViolationExceptionMapper.class);
        resources.add(EJBExceptionMapper.class);
        resources.add(OperationNotAllowedExceptionsMapper.class);
        resources.add(RSQLExceptionMapper.class);
        resources.add(UnauthorizedExceptionMapper.class);
        resources.add(ValidationExceptionExceptionMapper.class);
    }

    private void addSwaggerResources(Set<Class<?>> resources) {
        resources.add(OpenApiResource.class);
    }

    private void addMetricsResources(Set<Class<?>> resources) {
        resources.add(GeneralRestMetricsFilter.class);
        resources.add(TimedMetric.class);
        resources.add(TimedMetricFilter.class);
    }

    private void addRespondWithStatusFilter(Set<Class<?>> resources) {
        resources.add(RespondWithStatusFilter.class);
    }

    private void addProviders(Set<Class<?>> resources) {
        resources.add(JacksonProvider.class);
    }

}
