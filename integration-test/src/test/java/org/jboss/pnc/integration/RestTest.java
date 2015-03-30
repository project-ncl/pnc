package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int userId;

    private static Integer newProductId;
    private static Integer newProjectId;

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/product/";
    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/project/";
    private static final String PROJECT_REST_ENDPOINT_SPECIFIC = PROJECT_REST_ENDPOINT + "%d";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(0)
    public void shouldGetAllProducts() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/product").then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/product/%d", productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersions() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/product/%d/version", productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldSpecificProductsVersions() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/product/%d/version/%d", productId, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldGetFirstProject() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get("/pnc-rest/rest/project/").then()
                .statusCode(200).body(JsonMatcher.containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(5)
    public void shouldGetSpecificProject() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/project/%d", projectId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(6)
    public void shouldGetAllUsers() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/user").then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> userId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(7)
    public void shouldGetSpecificUser() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format("/pnc-rest/rest/user/%d", userId))
                .then().statusCode(200).body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(8)
    public void shouldCreateNewUser() {
        try {
            String rawJson = IoUtils.readFileOrResource("user", "user.json", getClass().getClassLoader());
            logger.info(rawJson);
            given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when().post("/pnc-rest/rest/user/").then()
                    .statusCode(201);

        } catch (IOException e) {
            Assertions.fail("Could not read user.json file", e);
        }
    }

    @Test
    @InSequence(9)
    public void shouldCreateNewProduct() {
        try {
            String rawJson = IoUtils.readFileOrResource("product", "product.json", getClass().getClassLoader());
            logger.info(rawJson);

            Response response = given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .post("/pnc-rest/rest/product/");
            Assertions.assertThat(response.statusCode()).isEqualTo(201);

            String location = response.getHeader("Location");
            logger.info("Found location in Response header: " + location);

            newProductId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_REST_ENDPOINT)
                    + PRODUCT_REST_ENDPOINT.length()));

            logger.info("Created id of product: " + newProductId);

        } catch (IOException e) {
            Assertions.fail("Could not read product.json file", e);
        }
    }

    @Test
    @InSequence(10)
    public void shouldUpdateProduct() {

        logger.info("### newProductId: " + newProductId);

        Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/product/%d", newProductId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(response.body().jsonPath().getString("name ")).isEqualTo(
                "JBoss Enterprise Application Platform 6");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("JBoss Enterprise Application Platform 6", "JBoss Enterprise Application Platform 7");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format("/pnc-rest/rest/product/%d", newProductId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/product/%d", newProductId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("name")).isEqualTo(
                "JBoss Enterprise Application Platform 7");

    }

    @Test
    @InSequence(11)
    public void shouldCreateNewProject() {

        try {
            String rawJson = IoUtils.readFileOrResource("project", "project.json", getClass().getClassLoader());
            logger.info(rawJson);

            Response response = given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort())
                    .header("Content-Type", "application/json; charset=UTF-8").when().post(PROJECT_REST_ENDPOINT);
            Assertions.assertThat(response.statusCode()).isEqualTo(201);

            String location = response.getHeader("Location");
            logger.info("Found location in Response header: " + location);

            newProjectId = Integer.valueOf(location.substring(location.lastIndexOf(PROJECT_REST_ENDPOINT)
                    + PROJECT_REST_ENDPOINT.length()));

            logger.info("Created id of project: " + newProjectId);

        } catch (IOException e) {
            Assertions.fail("Could not read project.json file", e);
        }
    }

    @Test
    @InSequence(12)
    public void shouldUpdateProject() {

        logger.info("### newProjectId: " + newProjectId);

        Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProjectId);
        Assertions.assertThat(response.body().jsonPath().getString("name")).isEqualTo("New Project");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("New Project", "New Awesome Project");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProjectId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("name")).isEqualTo("New Awesome Project");
    }
}
