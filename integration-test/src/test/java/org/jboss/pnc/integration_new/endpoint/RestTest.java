/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new.endpoint;

import io.restassured.http.ContentType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.json.JsonUtils;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.dto.Product;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration_new.setup.Credentials;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static io.restassured.RestAssured.given;
import io.restassured.specification.RequestSpecification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

/**
 * @author <a href="mailto:dbrazdil@redhat.com">Dominik Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String REST_PATH = "/pnc-rest-new/rest-new/";
    private static final String USER_REST_ENDPOINT = REST_PATH + "users/";
    private static final String PRODUCT_REST_ENDPOINT = REST_PATH + "products/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = REST_PATH + "product-versions/";
    private static final String PROJECT_REST_ENDPOINT = REST_PATH + "projects/";
    private static final String JSON_PATCH = "application/json-patch+json";
    private static final String FIRST_CONTENT_ID = "content[0].id";

    private static String productId;
    private static String productVersionId;
    private static String projectId;
    private static String newProductId;
    private static String newProjectId;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    private RequestSpecification givenCommonSettingAnd() {
        RequestSpecification request = given().accept(ContentType.JSON).port(getHttpPort());
        return Credentials.ADMIN.passCredentials(request.auth().preemptive()::basic);
    }

    @Test
    public void shouldGetAllProducts() {
        givenCommonSettingAnd().when()
                .get(PRODUCT_REST_ENDPOINT)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = value));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        givenCommonSettingAnd().when()
                .get(PRODUCT_REST_ENDPOINT + productId)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersionsForSpecificProduct() {
        givenCommonSettingAnd().when()
                .get(PRODUCT_REST_ENDPOINT + productId + "/versions")
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productVersionId = value));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductsVersions() {
        givenCommonSettingAnd().when()
                .get(PRODUCT_VERSION_REST_ENDPOINT + productVersionId)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id", value -> productVersionId = value));
    }

    @Test
    @InSequence(4)
    public void shouldGetAllProjects() {
        givenCommonSettingAnd().when()
                .get(PROJECT_REST_ENDPOINT)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> projectId = value));
    }

    @Test
    @InSequence(5)
    public void shouldGetSpecificProject() {
        givenCommonSettingAnd().when()
                .get(PROJECT_REST_ENDPOINT + projectId)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(6)
    public void shouldGetCurrentUser() {
        givenCommonSettingAnd().when()
                .get(USER_REST_ENDPOINT + "current")
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(7)
    public void shouldCreateNewProduct() throws IOException {
        String rawJson = IoUtils.readFileOrResource("product", "product.json", getClass().getClassLoader());

        givenCommonSettingAnd().body(rawJson)
                .contentType(ContentType.JSON)
                .when()
                .post(PRODUCT_REST_ENDPOINT)
                .then()
                .statusCode(201)
                .body(JsonMatcher.containsJsonAttribute("id", value -> newProductId = value));
    }

    @Test
    @InSequence(8)
    public void shouldUpdateProduct() throws Exception {
        Product product = givenCommonSettingAnd().when()
                .get(PRODUCT_REST_ENDPOINT + newProductId)
                .then()
                .statusCode(200)
                .extract()
                .as(Product.class);

        assertThat(product.getId()).isEqualTo(newProductId);

        // from product.json
        assertThat(product.getName()).isEqualTo("JBoss Enterprise Application Platform 6");

        String newName = "JBoss Enterprise Application Platform 7";
        product = product.toBuilder().name(newName).build();

        givenCommonSettingAnd().body(JsonUtils.toJson(product))
                .contentType(ContentType.JSON)
                .when()
                .put(PRODUCT_REST_ENDPOINT + newProductId)
                .then()
                .statusCode(204);

        // Reading updated resource
        givenCommonSettingAnd().when()
                .get(PRODUCT_REST_ENDPOINT + newProductId)
                .then()
                .statusCode(200)
                .body("id", equalTo(newProductId))
                .body("name", equalTo(newName));
    }

    @Test
    @InSequence(9)
    public void shouldCreateNewProject() throws Exception {
        String rawJson = IoUtils.readFileOrResource("project", "project.json", getClass().getClassLoader());

        givenCommonSettingAnd().body(rawJson)
                .contentType(ContentType.JSON)
                .when()
                .post(PROJECT_REST_ENDPOINT)
                .then()
                .statusCode(201)
                .body(JsonMatcher.containsJsonAttribute("id", value -> newProjectId = value));
    }

    @Test
    @InSequence(10)
    public void shouldUpdateProject() throws Exception {
        Project project = givenCommonSettingAnd().when()
                .get(PROJECT_REST_ENDPOINT + newProjectId)
                .then()
                .statusCode(200)
                .extract()
                .as(Project.class);

        assertThat(project.getId()).isEqualTo(newProjectId);
        // from project.json
        assertThat(project.getName()).isEqualTo("New Project");

        String newName = "New even more awesome project";
        project = project.toBuilder().name(newName).build();

        givenCommonSettingAnd().body(JsonUtils.toJson(project))
                .contentType(ContentType.JSON)
                .when()
                .put(PROJECT_REST_ENDPOINT + newProjectId)
                .then()
                .statusCode(204);

        // Reading updated resource
        givenCommonSettingAnd().when()
                .get(PROJECT_REST_ENDPOINT + newProjectId)
                .then()
                .statusCode(200)
                .body("id", equalTo(newProjectId))
                .body("name", equalTo(newName));
    }

}
