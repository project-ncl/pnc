/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.EnvironmentRestClient;
import org.jboss.pnc.integration.client.ProjectRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String CLONE_PREFIX_DATE_FORMAT = "yyyyMMddHHmmss";

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    private static final String PROJECT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/projects/%d";
    private static final String CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";
    private static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d";
    private static final String CONFIGURATION_DEPENDENCIES_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d/dependencies";
    private static final String CONFIGURATION_CLONE_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d/clone";
    private static final String ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environments";
    private static final String SPECIFIC_ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environments/%d";

    private static int productId;
    private static int projectId;
    private static int configurationId;
    private static int environmentId;

    private static AtomicBoolean isInitialized = new AtomicBoolean();

    private static AuthenticationProvider authProvider;
    private static String access_token = "no-auth";

    private static ProjectRestClient projectRestClient;
    private static EnvironmentRestClient environmentRestClient;
    private static BuildConfigurationRestClient buildConfigurationRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if (AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration
                    .getModuleConfig(new PncConfigProvider<>(AuthenticationModuleConfig.class));
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Before
    public void prepareData() throws Exception {
        if (!isInitialized.getAndSet(true)) {
            given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(CONFIGURATION_REST_ENDPOINT).then()
                    .statusCode(200).body(JsonMatcher.containsJsonAttribute("content[0].id",
                            value -> configurationId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> productId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(ENVIRONMENT_REST_ENDPOINT, productId)).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> environmentId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PROJECT_REST_ENDPOINT).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("content[0].id", value -> projectId = Integer.valueOf(value)));
        }

        if (projectRestClient == null) {
            projectRestClient = new ProjectRestClient();
        }
        if (environmentRestClient == null) {
            environmentRestClient = new EnvironmentRestClient();
        }
        if (buildConfigurationRestClient == null) {
            buildConfigurationRestClient = new BuildConfigurationRestClient();
        }
    }

    @Test
    public void shouldGetSpecificBuildConfiguration() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("content.id"));
    }

    @Test
    public void shouldCreateNewBuildConfiguration() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_create_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT).then().statusCode(201);
    }

    @Test
    public void shouldFailToCreateBuildConfigurationWhichDoesntMatchRegexp() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_create_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", ":");

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT).then().statusCode(400);
    }

    @Test
    public void shouldCreateNewBuildConfigurationWithCreateAndModifiedTime() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_WithEmptyCreateDate_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content.creationTime")
                .hasJsonValueNotNullOrEmpty("content.lastModificationTime");
    }

    @Test
    public void shouldUpdateBuildConfiguration() throws IOException {
        // given
        final String updatedScmUrl = "https://github.com/projects-ncl/pnc.git";
        final String updatedBuildScript = "mvn clean deploy -Dmaven.test.skip=true";
        final String updatedName = UUID.randomUUID().toString();
        final String updatedProjectId = String.valueOf(projectId);

        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_update_template");
        configurationTemplate.addValue("_name", updatedName);
        configurationTemplate.addValue("_buildScript", updatedBuildScript);
        configurationTemplate.addValue("_scmRepoURL", updatedScmUrl);
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositories", "");
        configurationTemplate.addValue("_projectId", updatedProjectId);
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));

        Response projectResponseBeforeTheUpdate = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, projectId));
        Response environmentResponseBeforeTheUpdate = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        // when
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        // then
        Response projectResponseAfterTheUpdate = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, projectId));
        Response environmentResponseAfterTheUpdate = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", configurationId)
                .hasJsonValueEqual("content.name", updatedName).hasJsonValueEqual("content.buildScript", updatedBuildScript)
                .hasJsonValueEqual("content.scmRepoURL", updatedScmUrl).hasJsonValueEqual("content.project.id", updatedProjectId)
                .hasJsonValueEqual("content.environment.id", environmentId);
        assertThat(projectResponseBeforeTheUpdate.getBody().print()).isEqualTo(projectResponseAfterTheUpdate.getBody().print());
        assertThat(environmentResponseBeforeTheUpdate.getBody().print())
                .isEqualTo(environmentResponseAfterTheUpdate.getBody().print());
    }

    @Test
    public void shouldCloneBuildConfiguration() {
        String buildConfigurationRestURI = String.format(CONFIGURATION_CLONE_REST_ENDPOINT, configurationId);
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body("").contentType(ContentType.JSON).port(getHttpPort()).when().post(buildConfigurationRestURI);

        String location = response.getHeader("Location");
        Integer clonedBuildConfigurationId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Cloned id of buildConfiguration: " + clonedBuildConfigurationId);

        Response originalBuildConfiguration = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        Response clonedBuildConfiguration = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, clonedBuildConfigurationId));

        ResponseAssertion.assertThat(response).hasStatus(201)
                .hasLocationMatches(".*\\/pnc-rest\\/rest\\/build-configurations\\/\\d+");

        assertThat(originalBuildConfiguration.body().jsonPath().getString("content.creationTime"))
                .isNotEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.creationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getInt("content.id"))
                .isNotEqualTo("_" + clonedBuildConfiguration.body().jsonPath().getInt("content.id"));

        assertThat("_" + originalBuildConfiguration.body().jsonPath().getString("content.name"))
                .isNotEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.name"));

        String prefix = clonedBuildConfiguration.body().jsonPath().getString("content.name").substring(0,
                clonedBuildConfiguration.body().jsonPath().getString("content.name").indexOf("_"));

        Date clonedBcPrefixDate = null;
        try {
            clonedBcPrefixDate = new SimpleDateFormat(CLONE_PREFIX_DATE_FORMAT).parse(prefix);
        } catch (ParseException ex) {
            clonedBcPrefixDate = null;
        }

        assertThat(clonedBcPrefixDate).isNotNull();

        assertThat(originalBuildConfiguration.body().jsonPath().getString("content.buildScript"))
                .isEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.buildScript"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("content.scmRepoURL"))
                .isEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.scmRepoURL"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("content.lastModificationTime"))
                .isEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.lastModificationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("content.repositories"))
                .isEqualTo(clonedBuildConfiguration.body().jsonPath().getString("content.repositories"));
    }

    @Test
    public void shouldFailToCreateNewBuildConfigurationBecauseIdIsNotNull() throws IOException {
        // given
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_with_id_template");
        configurationTemplate.addValue("_id", String.valueOf(Integer.MAX_VALUE));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositories", "");

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT).then().statusCode(400);
    }

    @Test
    public void shouldGetConflictWhenCreatingNewBuildConfigurationWithTheSameNameAndProjectId() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_WithEmptyCreateDate_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());

        Response firstAttempt = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT);

        Response secondAttempt = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT);

        ResponseAssertion.assertThat(firstAttempt).hasStatus(201);
        ResponseAssertion.assertThat(secondAttempt).hasStatus(409);
    }

    @Test
    public void shouldGetAuditedBuildConfigurations() throws Exception {
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT + "/revisions", configurationId));

        ResponseAssertion.assertThat(response).hasStatus(Status.OK.getStatusCode());
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content[0].id");
    }

    @Test
    public void shouldAddDependencyBuildConfiguration() throws Exception {
        // given
        RestResponse<ProjectRest> projectRestClient = this.projectRestClient.firstNotNull();
        RestResponse<BuildEnvironmentRest> environmentRestClient = this.environmentRestClient.firstNotNull();

        BuildConfigurationRest buildConfiguration = new BuildConfigurationRest();
        buildConfiguration.setName(UUID.randomUUID().toString());
        buildConfiguration.setProject(projectRestClient.getValue());
        buildConfiguration.setEnvironment(environmentRestClient.getValue());

        BuildConfigurationRest dependencyBuildConfiguration = new BuildConfigurationRest();
        dependencyBuildConfiguration.setName(UUID.randomUUID().toString());
        dependencyBuildConfiguration.setProject(projectRestClient.getValue());
        dependencyBuildConfiguration.setEnvironment(environmentRestClient.getValue());

        // when
        RestResponse<BuildConfigurationRest> configurationResponse = this.buildConfigurationRestClient
                .createNew(buildConfiguration);
        RestResponse<BuildConfigurationRest> depConfigurationResponse = this.buildConfigurationRestClient
                .createNew(dependencyBuildConfiguration);

        Integer configId = configurationResponse.getValue().getId();
        Integer depConfigId = depConfigurationResponse.getValue().getId();

        BuildConfigurationRestClient buildConfigDependencyRestClient = new BuildConfigurationRestClient();
        
        String buildConfigDepRestURI = String.format(CONFIGURATION_DEPENDENCIES_REST_ENDPOINT, configId);
        
        Response addDepResponse = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body("{ \"id\": " + depConfigId + " }").contentType(ContentType.JSON).port(getHttpPort()).when().post(buildConfigDepRestURI);

        RestResponse<BuildConfigurationRest> getUpdatedConfigResponse = this.buildConfigurationRestClient
                .get(configId);

        // then
        ResponseAssertion.assertThat(addDepResponse).hasStatus(200);
        assertThat(getUpdatedConfigResponse.getValue().getDependencyIds()).containsExactly(depConfigId);
    }

    @Test
    @InSequence(999)
    public void shouldDeleteBuildConfiguration() throws Exception {
        // given
        RestResponse<BuildConfigurationRest> configuration = this.buildConfigurationRestClient.firstNotNull();

        // when
        buildConfigurationRestClient.delete(configuration.getValue().getId());
        RestResponse<BuildConfigurationRest> returnedConfiguration = this.buildConfigurationRestClient
                .get(configuration.getValue().getId(), false);

        // then
        assertThat(returnedConfiguration.hasValue()).isEqualTo(false);
    }
}
