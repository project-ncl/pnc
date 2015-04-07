package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONObject;
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
public class BuildConfigurationSetRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/product/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version/";
    private static final String BUILD_CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/configuration/";

    private static final String BUILD_CONFIGURATION_SET_REST_ENDPOINT = "/pnc-rest/rest/configuration-set/";
    private static final String BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/configuration-set/%d";
    private static final String BUILD_CONFIGURATION_SET_PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version/%d/configuration-sets";
    private static final String BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT = "/pnc-rest/rest/configuration-set/%d/configurations";

    private static final String BUILD_CONFIGURATION_SET_NAME = "Rest Test Build Config Set 1";
    private static final String BUILD_CONFIGURATION_SET_NAME_UPDATED = "Rest Test Build Config Set 1 Updated";

    private static int productId;
    private static String productVersionName;
    private static int productVersionId;
    private static String buildConfName;
    private static int buildConfId;
    private static int buildConfId2;
    private static int newBuildConfSetId;
    
    private static AuthenticationProvider authProvider;
    

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(BuildConfigurationSetProvider.class);
        restWar.addClass(BuildConfigurationSetEndpoint.class);
        restWar.addClass(BuildConfigurationSetRest.class);
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        // Need to get a product version and a build configuration from the database
        ValidatableResponse responseProd = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));

        Response responseProdVer = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        ResponseAssertion.assertThat(responseProdVer).hasStatus(200);
        productVersionId = responseProdVer.body().jsonPath().getInt("[0].id");
        productVersionName = responseProdVer.body().jsonPath().getString("[0].version");

        Response responseBuildConf = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_CONFIGURATION_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildConf).hasStatus(200);
        buildConfId = responseBuildConf.body().jsonPath().getInt("[0].id");
        buildConfId2 = responseBuildConf.body().jsonPath().getInt("[1].id");
        buildConfName = responseBuildConf.body().jsonPath().getString("[0].name");

        logger.info("productVersionId: {} ", productVersionId);
        logger.info("productVersionName: {} ", productVersionName);
        logger.info("buildRecordId: {} ", buildConfId);
        logger.info("buildRecordName: {} ", buildConfName);
    }

    @Test
    @InSequence(1)
    public void testCreateNewBuildConfSet() throws IOException {
        JsonTemplateBuilder buildConfSetTemplate = JsonTemplateBuilder.fromResource("buildConfigurationSet_template");
        buildConfSetTemplate.addValue("_name", BUILD_CONFIGURATION_SET_NAME);
        buildConfSetTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        buildConfSetTemplate.addValue("_buildRecordIds", String.valueOf(buildConfId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().post(BUILD_CONFIGURATION_SET_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/configuration-set\\/\\d+");

        String location = response.getHeader("Location");
        newBuildConfSetId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Created id of BuildConfigurationSet: " + newBuildConfSetId);
    }

    @Test
    @InSequence(2)
    public void testUpdateBuildConfigurationSet() throws IOException {

        JsonTemplateBuilder buildConfSetTemplate = JsonTemplateBuilder.fromResource("buildConfigurationSet_template");
        buildConfSetTemplate.addValue("_name", BUILD_CONFIGURATION_SET_NAME_UPDATED);
        buildConfSetTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        buildConfSetTemplate.addValue("_buildRecordIds", String.valueOf(buildConfId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().put(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(2)
    public void testGetBuildConfigurationSets() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_CONFIGURATION_SET_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("[0].id");
    }

    @Test
    @InSequence(3)
    public void testGetSpecificBuildRecordSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("name", BUILD_CONFIGURATION_SET_NAME_UPDATED);
    }

    @Test
    @InSequence(4)
    public void testGetBuildConfigurationsForProductVersion() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_PRODUCT_VERSION_REST_ENDPOINT, productId, productVersionId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("[0].id");
    }

    @Test
    @InSequence(4)
    public void testAddBuildConfigurationToBuildConfigurationSet() {

        JSONObject buildConfig = new JSONObject();
        buildConfig.put("id", buildConfId2);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(buildConfig.toString())
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(5)
    public void testRemoveBuildConfigurationToBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT + "/%d", newBuildConfSetId, buildConfId2));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(5)
    public void testGetBuildConfigurationsForBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("[0].id", buildConfId);
    }

    @Test
    @InSequence(6)
    public void testDeleteBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

}