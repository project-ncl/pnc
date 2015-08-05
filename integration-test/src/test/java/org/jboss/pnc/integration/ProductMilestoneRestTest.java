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
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
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
public class ProductMilestoneRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/";
    private static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/";
    private static final String PRODUCT_MILESTONE_PRODUCTVERSION_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/product-versions/%d";
    private static final String PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT = PRODUCT_MILESTONE_REST_ENDPOINT + "%d";

    private static int productId;
    private static int productVersionId;
    private static int productMilestoneId;
    private static int newProductMilestoneId;

    private static AuthenticationProvider authProvider;
    private static String access_token = "no-auth";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if (AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(AuthenticationModuleConfig.class);
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(1)
    public void prepareProductIdAndProductVersionId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(2)
    public void prepareProductMilestoneId() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format(PRODUCT_MILESTONE_REST_ENDPOINT))
                .then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productMilestoneId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductMilestone() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, productMilestoneId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewProductMilestone() throws IOException {
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestone_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .body(productMilestoneTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_MILESTONE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        newProductMilestoneId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_MILESTONE_REST_ENDPOINT)
                + PRODUCT_MILESTONE_REST_ENDPOINT.length()));
        logger.info("Created id of product milestone: " + newProductMilestoneId);

    }

    @Test
    @InSequence(5)
    public void shouldUpdateProductMilestone() {

        logger.info("### newProductMilestoneId: " + newProductMilestoneId);

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductMilestoneId);
        Assertions.assertThat(response.body().jsonPath().getString("version ")).isEqualTo("1.0.0.ER1");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("1.0.0.ER1", "1.0.1.ER1");
        // Remove the "id: {id}," from the json object
        rawJson = rawJson.replaceFirst("\\s*\"?id\"?\\s*:\\s*\\d+,\\s*", "");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token).body(rawJson)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductMilestoneId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("version")).isEqualTo("1.0.1.ER1");

    }

    @Test
    @InSequence(6)
    public void shouldGetAllProductMilestoneOfProductVersion() {
        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_PRODUCTVERSION_REST_ENDPOINT, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    private String loadJsonFromFile(String resource) throws IOException {
        return IoUtils.readFileOrResource(resource, resource + ".json", getClass().getClassLoader());
    }
}
