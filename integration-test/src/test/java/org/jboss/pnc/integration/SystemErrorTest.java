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
import com.jayway.restassured.specification.RequestSpecification;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildConfigurationSetEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.response.Page;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 4/5/16
 * Time: 9:24 AM
 */
@Ignore // Test needs to be rewritten. Now it relies on broken test set-up to end with system error.
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class SystemErrorTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BUILD_CONFIGURATION_SET_REST_BUILD_ENDPOINT = "/pnc-rest/rest/build-configuration-sets/%d/build";
    private static final String BUILD_RECORD_LOG = "/pnc-rest/rest/build-records/%s/log";
    private static final String BUILD_CONFIGURATION_SET_NAME = "Rest Test Build Config Set 1";

    private static int productId;
    private static int productVersionId;
    private static int buildConfId;
    private static int newBuildConfSetId;
    private static int buildTaskId;

    private static BuildConfigurationSetRestClient buildConfigurationSetRestClient;
    private static UserRestClient userRestClient;


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);
        restWar.addClass(BuildConfigurationSetProvider.class);
        restWar.addClass(BuildConfigurationSetEndpoint.class);
        restWar.addClass(BuildConfigurationSetRest.class);
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);
        restWar.addAsWebInfResource("beans-use-mock-executor.xml", "beans.xml");

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-executor.xml", "beans.xml");

        addBuildExecutorMock(enterpriseArchive);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }
        if(userRestClient == null) {
            userRestClient = new UserRestClient();
            userRestClient.createUser("admin");
            userRestClient.createUser("user");
        }
    }

    @Test
    @InSequence(-1)
    @SuppressWarnings("unchecked")
    public void prepareBaseData() {
        userRestClient.getLoggedUser(); //initialize user

        // Need to get a product version and a build configuration from the database
        authenticatedJsonCall()
                .port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute(FIRST_CONTENT_ID, value -> productId = Integer.valueOf(value)));

        Response responseProdVer = authenticatedJsonCall()
                .port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId));
        ResponseAssertion.assertThat(responseProdVer).hasStatus(200);
        productVersionId = responseProdVer.body().jsonPath().getInt(FIRST_CONTENT_ID);

        Response responseBuildConf = authenticatedJsonCall()
                .port(getHttpPort()).when()
                .get(CONFIGURATION_REST_ENDPOINT);
        ResponseAssertion.assertThat(responseBuildConf).hasStatus(200);
        buildConfId = responseBuildConf.body().jsonPath().getInt(FIRST_CONTENT_ID);
    }

    @Test
    @InSequence(1)
    public void testCreateNewBuildConfSet() throws IOException {
        logger.info("Creating new Build Config Set. productId: {}, productVersionId: {}, buildConfId: {}.", productId, productVersionId, buildConfId);
        JsonTemplateBuilder buildConfSetTemplate = JsonTemplateBuilder.fromResource("buildConfigurationSet_template");
        buildConfSetTemplate.addValue("_name", BUILD_CONFIGURATION_SET_NAME);
        buildConfSetTemplate.addValue("_productVersionId", String.valueOf(productVersionId));
        buildConfSetTemplate.addValue("_buildRecordIds", String.valueOf(buildConfId));

        Response response = authenticatedJsonCall()
                .body(buildConfSetTemplate.fillTemplate())
                .port(getHttpPort()).when().post(BuildConfigurationSetRestClient.BUILD_CONFIGURATION_SET_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/build-configuration-sets\\/\\d+");

        String location = response.getHeader("Location");
        newBuildConfSetId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Created id of BuildConfigurationSet: " + newBuildConfSetId);
    }

    @Test
    @InSequence(2)
    public void testBuildNewConfSet() throws Exception {
        String url = String.format(BUILD_CONFIGURATION_SET_REST_BUILD_ENDPOINT, newBuildConfSetId) + "?rebuildAll=true";
        Response response = authenticatedJsonCall().post(url);

        ResponseAssertion.assertThat(response).hasStatus(200);
        @SuppressWarnings("unchecked")
        Page<Map<String, Object>> p = response.getBody().as(Page.class);

        logger.debug("Received: {}.", response.getBody().asString());

        buildTaskId = (Integer) p.getContent().iterator().next().get("id");
    }

    @Test
    @InSequence(3)
    public void shouldGetBuildData() throws Exception {
        Wait.forCondition(
                this::logHasCorrectFailedReason,
                4, ChronoUnit.SECONDS
        );
    }

    private boolean logHasCorrectFailedReason() {
        if (logger.isTraceEnabled()) {
            Response response = authenticatedJsonCall().get("/pnc-rest/rest/build-records");
            logger.trace("All build records: " + response.asString());
        }

        if (logger.isTraceEnabled()) { //all build logs should be without errors
            Response response = given().header("Accept", "text/plain").contentType(ContentType.JSON).get(String.format(BUILD_RECORD_LOG, buildTaskId));
            logger.trace("Build record log: " + response.asString());
        }

        Response response = given().header("Accept", "application/json").contentType(ContentType.JSON).get("/pnc-rest/rest/build-records/" + buildTaskId);
        logger.debug("Response string: " + response.asString());
        return response.getStatusCode() == 200
                && response.getBody().jsonPath().getString("content.status").equals("SYSTEM_ERROR");
    }

    private RequestSpecification authenticatedJsonCall() {
        return given().headers(testHeaders).contentType(ContentType.JSON);
    }
}
