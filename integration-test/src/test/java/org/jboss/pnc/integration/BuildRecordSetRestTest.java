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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.integration.utils.AuthUtils;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordSetEndpoint;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordSetRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/";
    private static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/build-records/";

    private static final String BUILD_RECORD_SET_REST_ENDPOINT = "/pnc-rest/rest/build-record-sets/";
    private static final String BUILD_RECORD_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-record-sets/%d";
    private static final String BUILD_RECORD_SET_BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/build-record-sets/build-records/%d";

    private static int performedInProductMilestoneId;
    private static String productMilestoneVersion;
    private static String buildRecordBuildScript;
    private static String buildRecordName;
    private static int buildRecordId;
    private static int newBuildRecordSetId;

    private static String access_token;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        restWar.addClass(BuildRecordSetProvider.class);
        restWar.addClass(BuildRecordSetEndpoint.class);
        restWar.addClass(BuildRecordSetRest.class);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        access_token = AuthUtils.generateToken();
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {
        Response responseProdMilestone = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(PRODUCT_MILESTONE_REST_ENDPOINT);

        ResponseAssertion.assertThat(responseProdMilestone).hasStatus(200);
        performedInProductMilestoneId = responseProdMilestone.body().jsonPath().getInt("content[0].id");
        productMilestoneVersion = responseProdMilestone.body().jsonPath().getString("content[0].version");

        Response responseBuildRec = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildRec).hasStatus(200);

        buildRecordId = responseBuildRec.body().jsonPath().getInt("content[0].id");
        buildRecordBuildScript = responseBuildRec.body().jsonPath().getString("content[0].buildScript");
        buildRecordName = responseBuildRec.body().jsonPath().getString("content[0].name");

        logger.info("performedInProductMilestoneId: {} ", performedInProductMilestoneId);
        logger.info("productMilestoneVersion: {} ", productMilestoneVersion);
        logger.info("buildRecordId: {} ", buildRecordId);
        logger.info("buildRecordBuildScript: {} ", buildRecordBuildScript);
        logger.info("buildRecordName: {} ", buildRecordName);
    }

    @Test
    @InSequence(1)
    public void shouldCreateNewBuildRecordSet() throws IOException {
        JsonTemplateBuilder buildRecordSetTemplate = JsonTemplateBuilder.fromResource("buildRecordSet_template");
        buildRecordSetTemplate.addValue("_performedInProductMilestoneId", String.valueOf(performedInProductMilestoneId));
        buildRecordSetTemplate.addValue("_buildRecordIds", String.valueOf(buildRecordId));

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .body(buildRecordSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().post(BUILD_RECORD_SET_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/build-record-sets\\/\\d+");

        String location = response.getHeader("Location");
        newBuildRecordSetId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Created id of BuildRecordSet: " + newBuildRecordSetId);
    }

    @Test
    @InSequence(2)
    public void shouldGetBuildRecordSets() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BUILD_RECORD_SET_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content[0].id");
    }

    @Test
    @InSequence(3)
    public void shouldGetSpecificBuildRecordSet() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SET_SPECIFIC_REST_ENDPOINT, newBuildRecordSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", newBuildRecordSetId);
    }

    @Test
    @InSequence(5)
    public void shouldGetBuildRecordSetForBuildRecord() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SET_BUILD_RECORD_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        List<Integer> buildRecordSetIds = response.getBody().jsonPath().getList("content.id");
        assertThat(buildRecordSetIds.contains(newBuildRecordSetId));
    }

}