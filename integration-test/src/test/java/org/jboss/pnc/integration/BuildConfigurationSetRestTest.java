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
import com.jayway.restassured.response.ValidatableResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.integration.Utils.AuthResource;
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
public class BuildConfigurationSetRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/";
    private static final String BUILD_CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";

    private static final String BUILD_CONFIGURATION_SET_REST_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/";
    private static final String BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/%d";
    private static final String BUILD_CONFIGURATION_SET_PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/%d/build-configuration-sets";
    private static final String BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/%d/build-configurations";

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
    private static String access_token =  "no-auth";
    

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        restWar.addClass(BuildConfigurationSetProvider.class);
        restWar.addClass(BuildConfigurationSetEndpoint.class);
        restWar.addClass(BuildConfigurationSetRest.class);
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);

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
    @InSequence(-1)
    public void prepareBaseData() {

        // Need to get a product version and a build configuration from the database
        ValidatableResponse responseProd = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));

        Response responseProdVer = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        ResponseAssertion.assertThat(responseProdVer).hasStatus(200);
        productVersionId = responseProdVer.body().jsonPath().getInt("[0].id");
        productVersionName = responseProdVer.body().jsonPath().getString("[0].version");

        Response responseBuildConf = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
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

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().post(BUILD_CONFIGURATION_SET_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/build-configuration-sets\\/\\d+");

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

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().put(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(2)
    public void testGetBuildConfigurationSets() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_CONFIGURATION_SET_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("[0].id");
    }

    @Test
    @InSequence(3)
    public void testGetSpecificBuildRecordSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("name", BUILD_CONFIGURATION_SET_NAME_UPDATED);
    }

    @Test
    @InSequence(4)
    public void testGetBuildConfigurationsForProductVersion() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
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

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(buildConfig.toString())
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(5)
    public void testRemoveBuildConfigurationToBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT + "/%d", newBuildConfSetId, buildConfId2));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(5)
    public void testGetBuildConfigurationsForBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("[0].id", buildConfId);
    }

    @Test
    @InSequence(6)
    public void testDeleteBuildConfigurationSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

}