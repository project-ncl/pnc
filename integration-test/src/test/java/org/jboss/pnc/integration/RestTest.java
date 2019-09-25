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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang.StringEscapeUtils;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.client.AbstractRestClient;
import org.jboss.pnc.integration.client.ProductVersionRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.common.json.JsonUtils;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String USER_REST_ENDPOINT = "/pnc-rest/rest/users/";
    private static final String SPECIFIC_USER_REST_ENDPOINT = USER_REST_ENDPOINT + "%d";
    private static final String NEW_AWESOME_PROJECT = "New Awesome Project";

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int userId;

    private static Integer newProductId;
    private static Integer newProjectId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }
    
    @Test
    @InSequence(0)
    public void shouldGetAllProducts() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_SPECIFIC_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersions() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(ProductVersionRestClient.PRODUCT_VERSION_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldSpecificProductsVersions() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(ProductVersionRestClient.PRODUCT_VERSION_REST_ENDPOINT + "%d", productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    @InSequence(4)
    public void shouldGetFirstProject() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(PROJECT_REST_ENDPOINT).then()
                .statusCode(200).body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(5)
    public void shouldGetSpecificProject() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, projectId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    @InSequence(5)
    public void shouldGetAllUsers() {
        final String toMatch = "{\"pageIndex\":0,\"pageSize\":50,\"totalPages\":1,\"content\":[{\"id\":100,\"email\":\"demo-user@pnc.com\",\"firstName\":\"Demo First Name\",\"lastName\":\"Demo Last Name\",\"username\":\"demo-user\"},{\"id\":101,\"email\":\"pnc-admin@pnc.com\",\"firstName\":\"pnc-admin\",\"lastName\":\"pnc-admin\",\"username\":\"pnc-admin\"}]}";

        given()
            .headers(testHeaders)
            .contentType(ContentType.JSON).port(getHttpPort()).when().get(USER_REST_ENDPOINT).then().assertThat()
            .body(equalTo(StringEscapeUtils.unescapeJava(toMatch)))
            .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> userId = Integer.valueOf(value)));

    }

    @Test
    @InSequence(7)
    public void shouldGetSpecificUser() {
        given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format(SPECIFIC_USER_REST_ENDPOINT, userId))
                .then().statusCode(200).body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    @InSequence(8)
    public void shouldCreateNewUser() throws IOException {
        String rawJson = IoUtils.readFileOrResource("user", "user.json", getClass().getClassLoader());
        logger.info(rawJson);
        given().headers(testHeaders)
                .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when().post(USER_REST_ENDPOINT).then()
                .statusCode(201);
    }

    @Test
    @InSequence(9)
    public void shouldCreateNewProduct() throws IOException {
        String rawJson = IoUtils.readFileOrResource("product", "product.json", getClass().getClassLoader());
        logger.info(rawJson);

        Response response = given().headers(testHeaders)
                .body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        newProductId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_REST_ENDPOINT)
                + PRODUCT_REST_ENDPOINT.length()));

        logger.info("Created id of product: " + newProductId);
    }

    @Test
    @InSequence(10)
    public void shouldUpdateProduct() throws Exception {
        logger.info("### newProductId: " + newProductId);

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_SPECIFIC_REST_ENDPOINT, newProductId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);

        ProductRest productRest = response.body().jsonPath().getObject(AbstractRestClient.CONTENT, ProductRest.class);

        Assertions.assertThat(productRest.getId()).isEqualTo(newProductId);
        Assertions.assertThat(productRest.getName()).isEqualTo("JBoss Enterprise Application Platform 6");

        productRest.setName("JBoss Enterprise Application Platform 7");

        given().headers(testHeaders)
                    .body(JsonUtils.toJson(productRest)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_SPECIFIC_REST_ENDPOINT, newProductId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_SPECIFIC_REST_ENDPOINT, newProductId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt(CONTENT_ID)).isEqualTo(newProductId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString(CONTENT_NAME)).isEqualTo(
                "JBoss Enterprise Application Platform 7");

    }

    @Test
    @InSequence(11)
    public void shouldCreateNewProject() throws Exception {
        String rawJson = IoUtils.readFileOrResource("project", "project.json", getClass().getClassLoader());
        logger.info(rawJson);

        Response response = given()
                .headers(testHeaders)
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
    public void shouldUpdateProject() throws Exception {
        logger.info("### newProjectId: " + newProjectId);

        Response response = given()
                .headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, newProjectId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);

        ProjectRest projectRest = response.body().jsonPath().getObject(AbstractRestClient.CONTENT, ProjectRest.class);

        Assertions.assertThat(projectRest.getId()).isEqualTo(newProjectId);
        Assertions.assertThat(projectRest.getName()).isEqualTo("New Project");

        projectRest.setName(NEW_AWESOME_PROJECT);

        given().headers(testHeaders)
                    .body(JsonUtils.toJson(projectRest)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, newProjectId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_SPECIFIC_REST_ENDPOINT, newProjectId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt(CONTENT_ID)).isEqualTo(newProjectId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString(CONTENT_NAME)).isEqualTo(NEW_AWESOME_PROJECT);
    }
}
