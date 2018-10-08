/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.AuthUtils;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
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
public class BuildRecordRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BUILD_RECORD_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-records/%d";
//    private static final String BUILD_RECORD_NAME_REST_ENDPOINT = "/pnc-rest/rest/build-records?q=latestBuildConfiguration.name==%s";
    private static final String BUILD_RECORD_PROJECT_REST_ENDPOINT = "/pnc-rest/rest/build-records/projects/%d";
//    private static final String BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT = "/pnc-rest/rest/build-records/projects/%d?q=latestBuildConfiguration.name==%s";
    private static final String BUILD_ENDPOINT_SSH_CREDENTIALS = "/pnc-rest/rest/builds/ssh-credentials/%d";

    private static int buildRecordId;
    private static int configurationId;
    private static String buildConfigurationName;
    private static int projectId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        //adding classes at runtime allows to "dynamically" add them to deployments without requiring to have new ear build
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);
        restWar.addClass(EndpointAuthenticationProvider.class);

        logger.info(enterpriseArchive.toString(true));
        logger.info("REST WAR: " + restWar.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {
        new UserRestClient().getLoggedUser();

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BuildRecordRestClient.BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        buildRecordId = response.body().jsonPath().getInt(FIRST_CONTENT_ID);
        configurationId = response.body().jsonPath().getInt("content[0].buildConfigurationId");

        logger.info("buildRecordId: {} ", buildRecordId);
        logger.info("configurationId: {} ", configurationId);

        response = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));
        ResponseAssertion.assertThat(response).hasStatus(200);
        buildConfigurationName = response.body().jsonPath().getString(CONTENT_NAME);

        UserRestClient userRestClient = new UserRestClient();
        userRestClient.createUser("admin");
        userRestClient.createUser("user");

        logger.info("buildConfigurationName: {} ", buildConfigurationName);
    }

    @Test
    public void shouldGetBuildRecords() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BuildRecordRestClient.BUILD_RECORD_REST_ENDPOINT  );
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual(FIRST_CONTENT_ID, buildRecordId);
    }

    @Test
    public void shouldGetSpecificBuildRecord() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SPECIFIC_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual(CONTENT_ID, buildRecordId);
    }

    @Test
    public void shouldGetAuditedConfigurationLinkedToBuildRecord() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SPECIFIC_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content.buildConfigurationAudited.genericParameters");
        BuildConfigurationAuditedRest bca = response.jsonPath().getObject("content.buildConfigurationAudited", BuildConfigurationAuditedRest.class);
        Assert.assertEquals("VALUE", bca.getGenericParameters().get("KEY"));
    }

    @Test
    @Ignore //TODO enable or delete
    public void shouldGetBuildRecordWithName() {
//        Response response = given().headers(testHeaders)
//                    .contentType(ContentType.JSON).port(getHttpPort()).when()
//                .get(String.format(BUILD_RECORD_NAME_REST_ENDPOINT, buildConfigurationName));
//
//        ResponseAssertion.assertThat(response).hasStatus(200);
//        ResponseAssertion.assertThat(response).hasJsonValueEqual(FIRST_CONTENT_ID, buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordForProject() {

        Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual(CONTENT_ID, configurationId);

        projectId = response.body().jsonPath().getInt("content.project.id");

        logger.info("projectId: {} ", projectId);

        Response response2 = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_PROJECT_REST_ENDPOINT, projectId));

        ResponseAssertion.assertThat(response2).hasStatus(200);
        ResponseAssertion.assertThat(response2).hasJsonValueEqual(FIRST_CONTENT_ID, buildRecordId);
    }

    @Test
    @Ignore //TODO enable or delete
    public void shouldGetBuildRecordForProjectWithName() {

//        Response response = given().headers(testHeaders)
//                    .contentType(ContentType.JSON).port(getHttpPort()).when()
//                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));
//
//        ResponseAssertion.assertThat(response).hasStatus(200);
//        ResponseAssertion.assertThat(response).hasJsonValueEqual(CONTENT_ID, configurationId);
//
//        projectId = response.body().jsonPath().getInt("content.project.id");
//
//        logger.info("projectId: {} ", projectId);
//
//        Response response2 = given().headers(testHeaders)
//                    .contentType(ContentType.JSON).port(getHttpPort()).when()
//                .get(String.format(BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT, projectId, buildConfigurationName));
//
//        ResponseAssertion.assertThat(response2).hasStatus(200);
//        ResponseAssertion.assertThat(response2).hasJsonValueEqual(FIRST_CONTENT_ID, buildRecordId);
    }

    @Test
    public void shouldFailToGetSshCredentialsForUserThatDidntTrigger() throws IOException {
        if (AuthUtils.authEnabled()) {
            Response response = given().headers(testHeaders)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(BUILD_ENDPOINT_SSH_CREDENTIALS, buildRecordId));

            ResponseAssertion.assertThat(response).hasStatus(204);
        }
    }

    @Test
    public void shouldFailToGetSshCredentialsForAnonymous() throws IOException {
        if (AuthUtils.authEnabled()) {
            Response response = given().header(acceptJsonHeader)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(BUILD_ENDPOINT_SSH_CREDENTIALS, buildRecordId));

            ResponseAssertion.assertThat(response).hasStatus(401);
        }
    }
}
