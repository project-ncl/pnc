package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
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
public class ProductVersionRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/product/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version/";
    private static final String PRODUCT_VERSION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/product/%d/version/%d";

    private static int productId;
    private static int productVersionId;
    private static int newProductVersionId;
    
    private static AuthenticationProvider authProvider;


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void prepareData() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    @InSequence(1)
    public void prepareProductId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(2)
    public void prepareProductVersionId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductVersion() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_SPECIFIC_REST_ENDPOINT, productId, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewProductVersion() throws IOException {
        String rawJson = loadJsonFromFile("productVersion");

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        logger.info("----1" + location.substring(location.lastIndexOf(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId))));

        logger.info("----2"
                + location.substring(location.lastIndexOf(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId))
                        + String.format(PRODUCT_VERSION_REST_ENDPOINT, productId).length()));

        newProductVersionId = Integer.valueOf(location.substring(location.lastIndexOf(String.format(
                PRODUCT_VERSION_REST_ENDPOINT, productId)) + String.format(PRODUCT_VERSION_REST_ENDPOINT, productId).length()));

        logger.info("Created id of product version: " + newProductVersionId);

    }

    @Test
    @InSequence(5)
    public void shouldUpdateProductVersion() {

        logger.info("### newProductVersionId: " + newProductVersionId);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_SPECIFIC_REST_ENDPOINT, productId, newProductVersionId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductVersionId);
        Assertions.assertThat(response.body().jsonPath().getString("version ")).isEqualTo("1.0.0.ER5");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("1.0.0.ER5", "1.0.1.ER1");
        // Remove the "id: {id}," from the json object
        rawJson = rawJson.replaceFirst("\\s*\"?id\"?\\s*:\\s*\\d+,\\s*", "");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_VERSION_SPECIFIC_REST_ENDPOINT, productId, newProductVersionId)).then()
                .statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_SPECIFIC_REST_ENDPOINT, productId, newProductVersionId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductVersionId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("version")).isEqualTo("1.0.1.ER1");

    }

    private String loadJsonFromFile(String resource) throws IOException {
        return IoUtils.readFileOrResource(resource, resource + ".json", getClass().getClassLoader());
    }
}
