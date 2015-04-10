package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
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

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProductReleaseRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/product/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version/";
    private static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestone/";
    private static final String PRODUCT_RELEASE_REST_ENDPOINT = "/pnc-rest/rest/product-release/";
    private static final String PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT = PRODUCT_RELEASE_REST_ENDPOINT + "%d";

    private static int productId;
    private static int productVersionId;
    private static int productMilestoneId;
    private static int productReleaseId;
    private static int newProductReleaseId;

    private static AuthenticationProvider authProvider;


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException {
        InputStream is = ProductReleaseRestTest.class.getResourceAsStream("/keycloak.json");
        ExternalAuthentication ea = new ExternalAuthentication(is);
        authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
    }

    @Test
    @InSequence(1)
    public void prepareProductIdAndProductVersionId() throws IOException {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));

        // Need to create a new product milestone to ensure one to one relation
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestone_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                .body(productMilestoneTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_MILESTONE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);
        String location = response.getHeader("Location");
        productMilestoneId = Integer.valueOf(location.substring(location.lastIndexOf(
                PRODUCT_MILESTONE_REST_ENDPOINT) + PRODUCT_MILESTONE_REST_ENDPOINT.length()));
    }

    @Test
    @InSequence(2)
    public void prepareProductReleaseId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_REST_ENDPOINT)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productReleaseId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductRelease() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, productReleaseId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewProductRelease() throws IOException {
        JsonTemplateBuilder productReleaseTemplate = JsonTemplateBuilder.fromResource("productRelease_template");
        productReleaseTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        productReleaseTemplate.addValue("_productMilestoneId", String.valueOf(productMilestoneId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                .body(productReleaseTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_RELEASE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        newProductReleaseId = Integer.valueOf(location.substring(location.lastIndexOf(
                PRODUCT_RELEASE_REST_ENDPOINT) + PRODUCT_RELEASE_REST_ENDPOINT.length()));

        logger.info("Created id of product version: " + newProductReleaseId);

    }

    @Test
    @InSequence(5)
    public void shouldUpdateProductRelease() {

        logger.info("### newProductReleaseId: " + newProductReleaseId);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductReleaseId);
        Assertions.assertThat(response.body().jsonPath().getString("version ")).isEqualTo("1.0.0.GA");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("1.0.0.GA", "1.0.1.GA");
        // Remove the "id: {id}," from the json object
        rawJson = rawJson.replaceFirst("\\s*\"?id\"?\\s*:\\s*\\d+,\\s*", "");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId)).then()
                .statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_RELEASE_SPECIFIC_REST_ENDPOINT, newProductReleaseId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductReleaseId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("version")).isEqualTo("1.0.1.GA");

    }

    private String loadJsonFromFile(String resource) throws IOException {
        return IoUtils.readFileOrResource(resource, resource + ".json", getClass().getClassLoader());
    }
}
