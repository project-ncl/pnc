package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/record/";
    private static final String BUILD_RECORD_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/record/%d";
    private static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/configuration/%d";
    private static final String BUILD_RECORD_NAME_REST_ENDPOINT = "/pnc-rest/rest/record?q=name==%s";
    private static final String BUILD_RECORD_PROJECT_REST_ENDPOINT = "/pnc-rest/rest/record/project/%d";
    private static final String BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT = "/pnc-rest/rest/record/project/%d?q=name==%s";

    private static int buildRecordId;
    private static int configurationId;
    private static String buildRecordName;
    private static int projectId;
    
    private static AuthenticationProvider authProvider;
    

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException {
        InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
        ExternalAuthentication ea = new ExternalAuthentication(is);
        authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BUILD_RECORD_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(200);
        buildRecordName = response.body().jsonPath().getString("[0].name");
        buildRecordId = response.body().jsonPath().getInt("[0].id");
        configurationId = response.body().jsonPath().getInt("[0].buildConfigurationId");

        logger.info("buildRecordName: {} ", buildRecordName);
        logger.info("buildRecordId: {} ", buildRecordId);
        logger.info("configurationId: {} ", configurationId);
    }

    @Test
    public void shouldGetBuildRecords() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("[0].id", buildRecordId);
    }

    @Test
    public void shouldGetSpecificBuildRecord() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SPECIFIC_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordWithName() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_NAME_REST_ENDPOINT, buildRecordName));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("[0].id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordForProject() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", configurationId);

        projectId = response.body().jsonPath().getInt("projectId");

        logger.info("projectId: {} ", projectId);

        Response response2 = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_PROJECT_REST_ENDPOINT, projectId));

        ResponseAssertion.assertThat(response2).hasStatus(200);
        ResponseAssertion.assertThat(response2).hasJsonValueEqual("[0].id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordForProjectWithName() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", configurationId);

        projectId = response.body().jsonPath().getInt("projectId");

        logger.info("projectId: {} ", projectId);

        Response response2 = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT, projectId, buildRecordName));

        ResponseAssertion.assertThat(response2).hasStatus(200);
        ResponseAssertion.assertThat(response2).hasJsonValueEqual("[0].id", buildRecordId);
    }

}
