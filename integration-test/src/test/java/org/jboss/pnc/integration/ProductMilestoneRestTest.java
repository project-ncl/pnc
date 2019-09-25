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
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.AbstractRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.common.json.JsonUtils;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
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
public class ProductMilestoneRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer productId = null;
    private static Integer productVersionId = null;
    private static int productMilestoneId;
    private static int newProductMilestoneId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void prepareProductIdAndProductVersionId() {
        if (productId == null) {
            given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = Integer.valueOf(value)));
        }

        if (productVersionId == null) {
            given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                    .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productVersionId = Integer.valueOf(value)));
        }
    }

    @Test
    @InSequence(2)
    public void prepareProductMilestoneId() {
        given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_MILESTONE_REST_ENDPOINT)
                .then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productMilestoneId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificProductMilestone() {
        given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, productMilestoneId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewProductMilestone() throws IOException {
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestone_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));

        Response response = given().headers(testHeaders)
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
    public void shouldFailToCreateExistingMilestone() throws IOException {
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestone_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));

        Response response = given().headers(testHeaders)
                .body(productMilestoneTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(PRODUCT_MILESTONE_REST_ENDPOINT);
        Assertions.assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    @InSequence(6)
    public void shouldUpdateProductMilestone() throws Exception {

        logger.info("### newProductMilestoneId: " + newProductMilestoneId);

        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt(CONTENT_ID)).isEqualTo(newProductMilestoneId);
        Assertions.assertThat(response.body().jsonPath().getString("content.version ")).isEqualTo("1.0.0.ER1");

        ProductMilestoneRest content = response.body().jsonPath().getObject(AbstractRestClient.CONTENT, ProductMilestoneRest.class);

        logger.info("### rawJson (before transformation): " + content);
        content.setVersion("1.0.1.ER1");
        logger.info("### rawJson (after transformation): " + content);

        given().headers(testHeaders).body(JsonUtils.toJson(content))
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().headers(testHeaders).contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT, newProductMilestoneId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt(CONTENT_ID)).isEqualTo(newProductMilestoneId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("content.version")).isEqualTo("1.0.1.ER1");

    }

    @Test
    @InSequence(7)
    public void shouldGetAllProductMilestoneOfProductVersion() {
        given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_MILESTONE_PRODUCTVERSION_REST_ENDPOINT, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(CONTENT_ID));
    }

    @Test
    public void shouldFailToCreateMilestoneWithMalformedVersion() throws IOException {
        // given
        JsonTemplateBuilder productMilestoneTemplate = JsonTemplateBuilder.fromResource("productMilestoneGeneric_template");
        productMilestoneTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        productMilestoneTemplate.addValue("_milestoneVersion", String.valueOf("1.0-ER1"));

        // when-then
        Response response1 = given().headers(testHeaders)
                .body(productMilestoneTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when().post(PRODUCT_MILESTONE_REST_ENDPOINT);
        Assertions.assertThat(response1.statusCode()).isEqualTo(400);
        response1.body().print();
        Assertions.assertThat(response1.body().jsonPath().getString("details.field"))
                .isEqualTo("version");
    }

}
