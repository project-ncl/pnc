/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.Utils.AuthResource;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

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

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    private static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    private static final String PROJECT_REST_ENDPOINT_SPECIFIC = PROJECT_REST_ENDPOINT + "%d";
    
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }
    
    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if(AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(AuthenticationModuleConfig.class);
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(0)
    public void shouldGetAllProducts() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/products").then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/products/%d", productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersions() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/products/%d/product-versions", productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldSpecificProductsVersions() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/products/%d/product-versions/%d", productId, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldGetFirstProject() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get("/pnc-rest/rest/projects/").then()
                .statusCode(200).body(JsonMatcher.containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(5)
    public void shouldGetSpecificProject() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/projects/%d", projectId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(6)
    public void shouldGetAllUsers() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/users").then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> userId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(7)
    public void shouldGetSpecificUser() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format("/pnc-rest/rest/users/%d", userId))
                .then().statusCode(200).body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(8)
    public void shouldCreateNewUser() {
        try {
            String rawJson = IoUtils.readFileOrResource("user", "user.json", getClass().getClassLoader());
            logger.info(rawJson);
            given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when().post("/pnc-rest/rest/users/").then()
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

            Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .post("/pnc-rest/rest/products/");
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

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/products/%d", newProductId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(response.body().jsonPath().getString("name ")).isEqualTo(
                "JBoss Enterprise Application Platform 6");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("JBoss Enterprise Application Platform 6", "JBoss Enterprise Application Platform 7");
        // Remove the "id: {id}," from the json object
        rawJson = rawJson.replaceFirst("\\s*\"?id\"?\\s*:\\s*\\d+,\\s*", "");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format("/pnc-rest/rest/products/%d", newProductId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-rest/rest/products/%d", newProductId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("name")).isEqualTo(
                "JBoss Enterprise Application Platform 7");

    }

    @Test
    @InSequence(11)
    public void shouldCreateNewProject() throws Exception {
        String rawJson = IoUtils.readFileOrResource("project", "project.json", getClass().getClassLoader());
        logger.info(rawJson);

        Response response = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .body(rawJson).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(PROJECT_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        newProjectId = Integer.valueOf(location.substring(location.lastIndexOf(PROJECT_REST_ENDPOINT)
                + PROJECT_REST_ENDPOINT.length()));

        logger.info("Created id of project: " + newProjectId);
    }

    @Test
    @InSequence(12)
    public void shouldUpdateProject() {
        logger.info("### newProjectId: " + newProjectId);

        Response response = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProjectId);
        Assertions.assertThat(response.body().jsonPath().getString("name")).isEqualTo("New Project");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("New Project", "New Awesome Project");
        // Remove the "id: {id}," from the json object
        rawJson = rawJson.replaceFirst("\\s*\"?id\"?\\s*:\\s*\\d+,\\s*", "");

        logger.info("### rawJson: " + rawJson);

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_REST_ENDPOINT_SPECIFIC, newProjectId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProjectId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("name")).isEqualTo("New Awesome Project");
    }
}
