package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/product/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version";
    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/project/";
    private static final String PROJECT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/project/%d";
    private static final String CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/configuration/";
    private static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/configuration/%d";
    private static final String CONFIGURATION_CLONE_REST_ENDPOINT = "/pnc-rest/rest/configuration/%d/clone";
    private static final String ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environment";
    private static final String SPECIFIC_ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environment/%d";
    private static final String CONFIGURATION_SET_REST_ENDPOINT = "/pnc-rest/rest/configuration-set/";
    private static final String CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/configuration-set/%d";

    private static int productId;
    private static int projectId;
    private static int configurationId;
    private static int environmentId;

    private static AtomicBoolean isInitialized = new AtomicBoolean();

    private static int configurationSetId;
    
    private static AuthenticationProvider authProvider;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException {
        InputStream is = BuildConfigurationRestTest.class.getResourceAsStream("/keycloak.json");
        ExternalAuthentication ea = new ExternalAuthentication(is);
        authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
    }

    @Before
    public void prepareData() {
        if (!isInitialized.getAndSet(true)) {
            given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(CONFIGURATION_REST_ENDPOINT).then()
                    .statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> configurationId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(ENVIRONMENT_REST_ENDPOINT, productId)).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> environmentId = Integer.valueOf(value)));

            given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(CONFIGURATION_SET_REST_ENDPOINT).then()
                    .statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute("[0].id", value -> configurationSetId = Integer.valueOf(value)));
        }
    }

    @Test
    @InSequence(-2)
    public void prepareProjectId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PROJECT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    public void shouldGetSpecificBuildConfiguration() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    public void shouldCreateNewBuildConfiguration() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_create_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when().post(
                CONFIGURATION_REST_ENDPOINT).then()
                .statusCode(201);
    }

    @Test
    public void shouldCreateNewBuildConfigurationWithCreateAndModifiedTime() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_WithEmptyCreateDate_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when().post(CONFIGURATION_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("creationTime").hasJsonValueNotNullOrEmpty("lastModificationTime");
    }

    @Test
    public void shouldUpdateBuildConfiguration() throws IOException {
        // given
        final String updatedScmUrl = "https://github.com/project-ncl/pnc.git";
        final String updatedBuildScript = "mvn clean deploy -Dmaven.test.skip=true";
        final String updatedName = "pnc-1.0.1.ER1";
        final String updatedProjectId = String.valueOf(projectId);

        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_update_template");
        configurationTemplate.addValue("_name", updatedName);
        configurationTemplate.addValue("_buildScript", updatedBuildScript);
        configurationTemplate.addValue("_scmRepoURL", updatedScmUrl);
        configurationTemplate.addValue("_patchesUrl", "");
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositories", "");
        configurationTemplate.addValue("_projectId", updatedProjectId);
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));

        Response projectResponseBeforeTheUpdate = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, projectId));
        Response environmentResponseBeforeTheUpdate = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));
        // when

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));


        // then
        Response projectResponseAfterTheUpdate = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, projectId));
        Response environmentResponseAfterTheUpdate = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", configurationId).hasJsonValueEqual("name", updatedName)
                .hasJsonValueEqual("buildScript", updatedBuildScript).hasJsonValueEqual("scmRepoURL", updatedScmUrl)
                .hasJsonValueEqual("projectId", updatedProjectId).hasJsonValueEqual("environmentId", environmentId);
        assertThat(projectResponseBeforeTheUpdate.getBody().print()).isEqualTo(projectResponseAfterTheUpdate.getBody().print());
        assertThat(environmentResponseBeforeTheUpdate.getBody().print()).isEqualTo(environmentResponseAfterTheUpdate.getBody().print());
    }

    @Test
    public void shouldCloneBuildConfiguration() {
        String buildConfigurationRestURI = String.format(CONFIGURATION_CLONE_REST_ENDPOINT, configurationId);
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body("").contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(buildConfigurationRestURI);

        String location = response.getHeader("Location");
        Integer clonedBuildConfigurationId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Cloned id of buildConfiguration: " + clonedBuildConfigurationId);

        Response originalBuildConfiguration = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        Response clonedBuildConfiguration = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, clonedBuildConfigurationId));

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/configuration\\/\\d+");

        assertThat(originalBuildConfiguration.body().jsonPath().getString("creationTime")).isNotEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("creationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getInt("id")).isNotEqualTo(
                "_" + clonedBuildConfiguration.body().jsonPath().getInt("id"));

        assertThat("_" + originalBuildConfiguration.body().jsonPath().getString("name")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("name"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("buildScript")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("buildScript"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("scmRepoURL")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("scmRepoURL"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("patchesUrl")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("patchesUrl"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("lastModificationTime")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("lastModificationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("repositories")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("repositories"));
    }

    @Test
    public void shouldFailToCreateNewBuildConfiguration() throws IOException {
        // given
        final String scmUrl = "https://github.com/project-ncl/pnc.git";
        final String buildScript = "mvn clean deploy -Dmaven.test.skip=true";
        final String name = "Bad Request Example Config";
        final String id = String.valueOf(projectId);

        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_with_id_template");
        configurationTemplate.addValue("_id", String.valueOf(Integer.MAX_VALUE));
        configurationTemplate.addValue("_name", name);
        configurationTemplate.addValue("_buildScript", buildScript);
        configurationTemplate.addValue("_scmRepoURL", scmUrl);
        configurationTemplate.addValue("_patchesUrl", "");
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositories", "");
        configurationTemplate.addValue("_projectId", id);
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(CONFIGURATION_REST_ENDPOINT).then().statusCode(400);
    }

    @Test
    @InSequence(999)
    public void shouldDeleteBuildConfiguration() throws Exception {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .delete(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200);
    }
}
