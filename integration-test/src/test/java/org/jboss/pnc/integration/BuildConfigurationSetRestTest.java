/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigSetRecordRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationSetRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/%d";
    private static final String BUILD_CONFIGURATION_SET_PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/product-versions/%d/build-configuration-sets";
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

    private static boolean setupComplete = false;
    private static BuildConfigurationSetRest bcSetRest1;
    private static BuildConfigurationSetRest bcSetRest2;
    private static BuildConfigurationRest bcRest1;
    private static BuildConfigurationRest bcRest2;

    private static UserRestClient userRestClient;
    private static BuildConfigSetRecordRestClient buildConfigSetRecordRestClient;

    private static BuildConfigurationSetRestClient buildConfigurationSetRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(BuildConfigurationSetProvider.class);
        restWar.addClass(BuildConfigurationSetEndpoint.class);
        restWar.addClass(BuildConfigurationSetRest.class);
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);

        restWar.addAsWebInfResource("beans-use-mock-remote-clients.xml", "beans.xml");

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        addBuildExecutorMock(enterpriseArchive);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }

        if(buildConfigSetRecordRestClient == null) {
            buildConfigSetRecordRestClient = new BuildConfigSetRecordRestClient();
        }

        if(userRestClient == null) {
            userRestClient = new UserRestClient();
            userRestClient.createUser("admin");
            userRestClient.createUser("user");
        }
        // Setup data for build-configurations sub-endpoint tests
        if (!setupComplete) {
            setupComplete = true;
//            ProjectRest projectRest = new ProjectRestClient().firstNotNull();
//
//            BuildConfigurationRestClient buildConfigurationRestClient = new BuildConfigurationRestClient();
//            bcRest1 = new BuildConfigurationRest();
//            bcRest1.setName("BuildConfigurationSetRestTest-bc-1");
//            bcRest1.setProject(projectRest);
//            bcRest1 = buildConfigurationRestClient.createNewRCAndBC(bcRest1).getValue();
//
//            bcRest2 = new BuildConfigurationRest();
//            bcRest2.setName("BuildConfigurationSetRestTest-bc-2");
//            bcRest2.setProject(projectRest);
//            bcRest2 = buildConfigurationRestClient.createNewRCAndBC(bcRest2).getValue();

            List<BuildConfigurationRest> bcs = new BuildConfigurationRestClient().all(false, 0, 2, null, null).getValue();
            bcRest1 = bcs.get(0);
            bcRest2 = bcs.get(1);

            bcSetRest1 = new BuildConfigurationSetRest();
            bcSetRest1.setName("BuildConfigurationSetRestTest-bcset-1");
            bcSetRest1.addBuildConfiguration(bcRest1);
            bcSetRest1 = buildConfigurationSetRestClient.createNew(bcSetRest1).getValue();

            bcSetRest2 = new BuildConfigurationSetRest();
            bcSetRest2.setName("BuildConfigurationSetRestTest-bcset-2");
            bcSetRest2.addBuildConfiguration(bcRest2);
            bcSetRest2 = buildConfigurationSetRestClient.createNew(bcSetRest2).getValue();
        }
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {

        // Need to get a product version and a build configuration from the database
        ValidatableResponse responseProd = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = Integer.valueOf(value)));

        Response responseProdVer = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        ResponseAssertion.assertThat(responseProdVer).hasStatus(200);
        productVersionId = responseProdVer.body().jsonPath().getInt(FIRST_CONTENT_ID);
        productVersionName = responseProdVer.body().jsonPath().getString("content[0].version");

        Response responseBuildConf = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(CONFIGURATION_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildConf).hasStatus(200);
        buildConfId = responseBuildConf.body().jsonPath().getInt(FIRST_CONTENT_ID);
        buildConfId2 = responseBuildConf.body().jsonPath().getInt("content[1].id");
        buildConfName = responseBuildConf.body().jsonPath().getString("content[0].name");

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

        Response response = given().headers(testHeaders)
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().post(BuildConfigurationSetRestClient.BUILD_CONFIGURATION_SET_REST_ENDPOINT);

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

        Response response = given().headers(testHeaders)
                    .body(buildConfSetTemplate.fillTemplate()).contentType(ContentType.JSON)
                .port(getHttpPort()).when().put(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(2)
    public void testGetBuildConfigurationSets() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(BuildConfigurationSetRestClient.BUILD_CONFIGURATION_SET_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content[0]");
    }

    @Test
    @InSequence(3)
    public void testGetSpecificBuildRecordSet() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual(CONTENT_NAME, BUILD_CONFIGURATION_SET_NAME_UPDATED);
    }

    @Test
    @InSequence(4)
    public void testGetBuildConfigurationsForProductVersion() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_PRODUCT_VERSION_REST_ENDPOINT, productVersionId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty(FIRST_CONTENT_ID);
    }

    @Test
    @InSequence(4)
    public void testAddBuildConfigurationToBuildConfigurationSet() {
        JSONObject buildConfig = new JSONObject();
        buildConfig.put("id", buildConfId2);

        Response response = given().headers(testHeaders)
                    .body(buildConfig.toString())
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    /**
     * reproducer for NCL-3552
     */
    @Test
    @InSequence(5)
    public void testConcurrentGet() {
        ExecutorService executorService = MDCExecutors.newFixedThreadPool(2);

        Map<Integer, Response> responseMap = new HashMap<>();

        String endpointUrl = String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId);
        executorService.execute(() -> {
                    logger.info("Making 1st request ...");
                    Response response = given().headers(testHeaders)
                            .contentType(ContentType.JSON).port(getHttpPort()).when()
                            .get(endpointUrl);
                    logger.info("1st done.");
                    responseMap.put(1, response);
                }
        );

        executorService.execute(() -> {
                    logger.info("Making 2nd request ...");
                    Response response = given().headers(testHeaders)
                            .contentType(ContentType.JSON).port(getHttpPort()).when()
                            .get(endpointUrl);
                    logger.info("2nd done.");
                    responseMap.put(2, response);
                }
        );


        // Wait for all the threads to gracefully shutdown
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new AssertionError("Requests were not completed in given timeout.", e);
        }

        ResponseAssertion.assertThat(responseMap.get(1)).as("First response.").hasStatus(200);
        ResponseAssertion.assertThat(responseMap.get(2)).as("Second response.").hasStatus(200);
    }

    @Test
    @InSequence(6)
    public void testRemoveBuildConfigurationToBuildConfigurationSet() {

        Response response = given().headers(testHeaders)
                    .port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT + "/%d", newBuildConfSetId, buildConfId2));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    @InSequence(7)
    public void testGetBuildConfigurationsForBuildConfigurationSet() {

        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual(FIRST_CONTENT_ID, buildConfId);
    }

    @Test
    @InSequence(8)
    public void testDeleteBuildConfigurationSet() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .delete(String.format(BUILD_CONFIGURATION_SET_SPECIFIC_REST_ENDPOINT, newBuildConfSetId));

        ResponseAssertion.assertThat(response).hasStatus(200);
    }

    @Test
    public void testIfCreatingNewBuildConfigurationSetFailsBecauseTheNameAlreadyExists() throws Exception {
        //given
        BuildConfigurationSetRest project = new BuildConfigurationSetRest();
        project.setName(UUID.randomUUID().toString());

        //when
        RestResponse<BuildConfigurationSetRest> firstResponse = buildConfigurationSetRestClient.createNew(project);
        RestResponse<BuildConfigurationSetRest> secondResponse = buildConfigurationSetRestClient.createNew(project, false);

        //than
        assertThat(firstResponse.hasValue()).isEqualTo(true);
        assertThat(secondResponse.getRestCallResponse().getStatusCode()).isEqualTo(409);
    }

    @Test
    public void shouldUpdateAllBuildConfigurations() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRestList = new LinkedList<>();
        buildConfigurationRestList.add(bcRest2);

        //when
        RestResponse<List<BuildConfigurationRest>> response = buildConfigurationSetRestClient.updateBuildConfigurations(bcSetRest1.getId(), buildConfigurationRestList, true);

        //then
        assertThat(response.getValue().stream().map(BuildConfigurationRest::getId).collect(Collectors.toList())).containsOnly(bcRest2.getId());
    }

    @Test
    public void shouldUpdateAllBuildConfigurationsWithEmptyList() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRestList = new LinkedList<>();

        //when
        RestResponse<List<BuildConfigurationRest>> response = buildConfigurationSetRestClient.updateBuildConfigurations(bcSetRest1.getId(), buildConfigurationRestList, false);

        //then
        assertThat(response.getRestCallResponse().statusCode()).isEqualTo(200);
        assertThat(response.getValue()).isNullOrEmpty();
    }

    @Test
    @InSequence(1)
    public void testIfBuildConfigSetIsArchived() throws Exception {
        //when
        userRestClient.getLoggedUser(); //initialize user
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        RestResponse<BuildConfigSetRecordRest> buildResponse = buildConfigurationSetRestClient.trigger(bcSetRest2.getId(), buildOptions);
        Integer buildRecordSetId = ResponseUtils.getIdFromLocationHeader(buildResponse.getRestCallResponse());

        assertThat(buildResponse.getRestCallResponse().getStatusCode()).isEqualTo(200);
        assertThat(buildRecordSetId).isNotNull();

        //wait until build is done
        ResponseUtils.waitSynchronouslyFor(() -> buildConfigSetRecordRestClient.get(buildRecordSetId, false).hasValue(), 15, TimeUnit.SECONDS);
        //deleting the Set should turn out in archival of it
        buildConfigurationSetRestClient.delete(bcSetRest2.getId(), true);

        //then
        RestResponse<BuildConfigurationSetRest> archivedBCS = buildConfigurationSetRestClient.get(bcSetRest2.getId(),true);
        assertThat(archivedBCS.hasValue()).isTrue();
        assertThat(archivedBCS.getValue().isArchived()).isTrue();
    }

    @Test @InSequence(2) public void testIfLinksOfArchivedBuildConfigSetAreDeleted() throws Exception {
        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_SET_CONFIGURATIONS_REST_ENDPOINT, bcSetRest2.getId()));

        //204 = success but no content
        ResponseAssertion.assertThat(response).hasStatus(204);
    }

    @Test @InSequence(3) public void testReconstructingOfArchivedBuildConfigSet() throws Exception {
        RestResponse<BuildConfigurationSetRest> reconstructedBCS = buildConfigurationSetRestClient.get(bcSetRest2.getId(), true);

        assertThat(reconstructedBCS.hasValue()).isTrue();
        assertThat(reconstructedBCS.getValue().getBuildConfigurationIds()).contains(bcRest2.getId());
    }
}