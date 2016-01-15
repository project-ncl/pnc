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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.endpoint.BuildRecordEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
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

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/build-records/";
    private static final String BUILD_RECORD_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-records/%d";
    private static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d";
    private static final String BUILD_RECORD_NAME_REST_ENDPOINT = "/pnc-rest/rest/build-records?q=latestBuildConfiguration.name==%s";
    private static final String BUILD_RECORD_PROJECT_REST_ENDPOINT = "/pnc-rest/rest/build-records/projects/%d";
    private static final String BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT = "/pnc-rest/rest/build-records/projects/%d?q=latestBuildConfiguration.name==%s";

    private static int buildRecordId;
    private static int configurationId;
    private static String buildConfigurationName;
    private static int projectId;
    
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";
    

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        //adding classes at runtime allows to "dynamically" add them to deployments without requiring to have new ear build
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);
        restWar.addClass(BuildRecordProvider.class);
        restWar.addClass(BuildRecordEndpoint.class);
        restWar.addClass(BuildRecordRest.class);

        logger.info(enterpriseArchive.toString(true));
        logger.info("REST WAR: " + restWar.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if(AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class));
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(-1)
    public void prepareBaseData() {
        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        buildRecordId = response.body().jsonPath().getInt("content[0].id");
        configurationId = response.body().jsonPath().getInt("content[0].buildConfigurationId");

        logger.info("buildRecordId: {} ", buildRecordId);
        logger.info("configurationId: {} ", configurationId);

        response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));
        ResponseAssertion.assertThat(response).hasStatus(200);
        buildConfigurationName = response.body().jsonPath().getString("content.name");

        logger.info("buildConfigurationName: {} ", buildConfigurationName);
    }

    @Test
    public void shouldGetBuildRecords() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when().get(BUILD_RECORD_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content[0].id", buildRecordId);
    }

    @Test
    public void shouldGetSpecificBuildRecord() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_SPECIFIC_REST_ENDPOINT, buildRecordId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordWithName() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_NAME_REST_ENDPOINT, buildConfigurationName));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content[0].id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordForProject() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", configurationId);

        projectId = response.body().jsonPath().getInt("content.project.id");

        logger.info("projectId: {} ", projectId);

        Response response2 = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_PROJECT_REST_ENDPOINT, projectId));

        ResponseAssertion.assertThat(response2).hasStatus(200);
        ResponseAssertion.assertThat(response2).hasJsonValueEqual("content[0].id", buildRecordId);
    }

    @Test
    public void shouldGetBuildRecordForProjectWithName() {

        Response response = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", configurationId);

        projectId = response.body().jsonPath().getInt("content.project.id");

        logger.info("projectId: {} ", projectId);

        Response response2 = given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_RECORD_PROJECT_BR_NAME_REST_ENDPOINT, projectId, buildConfigurationName));

        ResponseAssertion.assertThat(response2).hasStatus(200);
        ResponseAssertion.assertThat(response2).hasJsonValueEqual("content[0].id", buildRecordId);
    }

}
