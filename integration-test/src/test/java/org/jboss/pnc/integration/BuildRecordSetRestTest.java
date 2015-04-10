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
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordSetEndpoint;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class BuildRecordSetRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestone/";
    private static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/record/";

    private static final String BUILD_RECORD_SET_REST_ENDPOINT = "/pnc-rest/rest/recordset/";
    private static final String BUILD_RECORD_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/recordset/%d";
    private static final String BUILD_RECORD_SET_PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/recordset/productversion/%d";
    private static final String BUILD_RECORD_SET_BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/recordset/record/%d";

    private static int productMilestoneId;
    private static String productMilestoneVersion;
    private static String buildRecordBuildScript;
    private static String buildRecordName;
    private static int buildRecordId;
    private static int newBuildRecordSetId;

    private static AuthenticationProvider authProvider;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(BuildRecordSetProvider.class);
        restWar.addClass(BuildRecordSetEndpoint.class);
        restWar.addClass(BuildRecordSetRest.class);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);

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

        Response responseProdMilestone = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(PRODUCT_MILESTONE_REST_ENDPOINT);

        ResponseAssertion.assertThat(responseProdMilestone).hasStatus(200);
        productMilestoneId = responseProdMilestone.body().jsonPath().getInt("[0].id");
        productMilestoneVersion = responseProdMilestone.body().jsonPath().getString("[0].version");

        Response responseBuildRec = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildRec).hasStatus(200);

        buildRecordId = responseBuildRec.body().jsonPath().getInt("[0].id");
        buildRecordBuildScript = responseBuildRec.body().jsonPath().getString("[0].buildScript");
        buildRecordName = responseBuildRec.body().jsonPath().getString("[0].name");

        logger.info("productMilestoneId: {} ", productMilestoneId);
        logger.info("productMilestoneVersion: {} ", productMilestoneVersion);
        logger.info("buildRecordId: {} ", buildRecordId);
        logger.info("buildRecordBuildScript: {} ", buildRecordBuildScript);
        logger.info("buildRecordName: {} ", buildRecordName);
    }

    @Test
    @InSequence(1)
    public void shouldCreateNewBuildRecordSet() throws IOException {
        JsonTemplateBuilder buildRecordSetTemplate = JsonTemplateBuilder.fromResource("buildRecordSet_template");
        buildRecordSetTemplate.addValue("_productMilestoneId", String.valueOf(productMilestoneId));
        buildRecordSetTemplate.addValue("_buildRecordIds", String.valueOf(buildRecordId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(buildRecordSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().post(BUILD_RECORD_SET_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/recordset\\/\\d+");

        String location = response.getHeader("Location");
        newBuildRecordSetId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Created id of BuildRecordSet: " + newBuildRecordSetId);
    }

    @Test
    @InSequence(2)
    public void shouldGetBuildRecordSets() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_RECORD_SET_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("[0].id");
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificBuildRecordSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SET_SPECIFIC_REST_ENDPOINT, newBuildRecordSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", newBuildRecordSetId);
    }

    @Test
    @InSequence(5)
    public void shouldGetBuildRecordForBuildRecord() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SET_BUILD_RECORD_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("[0].id", newBuildRecordSetId);
    }

}