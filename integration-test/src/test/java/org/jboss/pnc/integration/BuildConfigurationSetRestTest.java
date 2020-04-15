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
package org.jboss.pnc.integration;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigSetRecordRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationSetRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static String productVersionName;
    private static int productVersionId;
    private static String buildConfName;
    private static int buildConfId;

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
        if (buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }

        if (buildConfigSetRecordRestClient == null) {
            buildConfigSetRecordRestClient = new BuildConfigSetRecordRestClient();
        }

        if (userRestClient == null) {
            userRestClient = new UserRestClient();
            userRestClient.createUser("admin");
            userRestClient.createUser("user");
        }
        // Setup data for build-configurations sub-endpoint tests
        if (!setupComplete) {
            setupComplete = true;
            // ProjectRest projectRest = new ProjectRestClient().firstNotNull();
            //
            // BuildConfigurationRestClient buildConfigurationRestClient = new BuildConfigurationRestClient();
            // bcRest1 = new BuildConfigurationRest();
            // bcRest1.setName("BuildConfigurationSetRestTest-bc-1");
            // bcRest1.setProject(projectRest);
            // bcRest1 = buildConfigurationRestClient.createNewRCAndBC(bcRest1).getValue();
            //
            // bcRest2 = new BuildConfigurationRest();
            // bcRest2.setName("BuildConfigurationSetRestTest-bc-2");
            // bcRest2.setProject(projectRest);
            // bcRest2 = buildConfigurationRestClient.createNewRCAndBC(bcRest2).getValue();

            List<BuildConfigurationRest> bcs = new BuildConfigurationRestClient().all(false, 0, 2, null, null)
                    .getValue();
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
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(PRODUCT_REST_ENDPOINT)
                .then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = Integer.valueOf(value)));

        Response responseProdVer = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        ResponseAssertion.assertThat(responseProdVer).hasStatus(200);
        productVersionId = responseProdVer.body().jsonPath().getInt(FIRST_CONTENT_ID);
        productVersionName = responseProdVer.body().jsonPath().getString("content[0].version");

        Response responseBuildConf = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(CONFIGURATION_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildConf).hasStatus(200);
        buildConfId = responseBuildConf.body().jsonPath().getInt(FIRST_CONTENT_ID);
        buildConfName = responseBuildConf.body().jsonPath().getString("content[0].name");

        logger.info("productVersionId: {} ", productVersionId);
        logger.info("productVersionName: {} ", productVersionName);
        logger.info("buildRecordId: {} ", buildConfId);
        logger.info("buildRecordName: {} ", buildConfName);
    }

    @Test
    public void shouldUpdateAllBuildConfigurations() throws Exception {
        // given
        List<BuildConfigurationRest> buildConfigurationRestList = new LinkedList<>();
        buildConfigurationRestList.add(bcRest2);

        // when
        RestResponse<List<BuildConfigurationRest>> response = buildConfigurationSetRestClient
                .updateBuildConfigurations(bcSetRest1.getId(), buildConfigurationRestList, true);

        // then
        assertThat(response.getValue().stream().map(BuildConfigurationRest::getId).collect(Collectors.toList()))
                .containsOnly(bcRest2.getId());
    }

    @Test
    public void shouldUpdateAllBuildConfigurationsWithEmptyList() throws Exception {
        // given
        List<BuildConfigurationRest> buildConfigurationRestList = new LinkedList<>();

        // when
        RestResponse<List<BuildConfigurationRest>> response = buildConfigurationSetRestClient
                .updateBuildConfigurations(bcSetRest1.getId(), buildConfigurationRestList, false);

        // then
        assertThat(response.getRestCallResponse().statusCode()).isEqualTo(200);
        assertThat(response.getValue()).isNullOrEmpty();
    }

}