/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.rest.endpoint.BuildEnvironmentEndpoint;
import org.jboss.pnc.rest.provider.BuildEnvironmentProvider;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
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
import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.jboss.pnc.integration.utils.JsonUtils.toJson;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class EnvironmentRestTest {

    private static final String ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environments/";
    private static final String ENVIRONMENT_REST_ENDPOINT_SPECIFIC = ENVIRONMENT_REST_ENDPOINT + "%d";

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer environmentId;
    
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";
    

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        restWar.addClass(BuildEnvironmentProvider.class);
        restWar.addClass(BuildEnvironmentEndpoint.class);
        restWar.addClass(BuildEnvironmentRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }
    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if(AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(AuthenticationModuleConfig.class));
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(0)
    public void shouldCreateNewEnvironment() throws Exception {
        //given
        String environment = toJson(exampleEnvironment());

        //when
        Response response = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .body(environment).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(ENVIRONMENT_REST_ENDPOINT);
        environmentId = ResponseUtils.getIdFromLocationHeader(response);

        //then
        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/environments\\/\\d+");
        assertThat(environmentId).isNotNull();
    }

    @Test
    @InSequence(1)
    public void shouldUpdateEnvironment() throws Exception {
        //given
        BuildEnvironmentRest environmentModified = exampleEnvironment();
        environmentModified.setBuildType(BuildType.JAVA);

        //when
        Response putResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .body(toJson(environmentModified)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        Response getResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        BuildEnvironmentRest noLoremIpsum = getResponse.jsonPath().getObject("content", BuildEnvironmentRest.class);

        //then
        ResponseAssertion.assertThat(putResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(200);
        assertThat(noLoremIpsum.getBuildType()).isEqualTo(BuildType.JAVA);
    }

    @Test
    @InSequence(2)
    public void shouldDeleteEnvironment() throws Exception {
        //when
        Response deleteResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).port(getHttpPort()).when()
                .delete(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        Response getResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        //then
        ResponseAssertion.assertThat(deleteResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(404);
    }

    private BuildEnvironmentRest exampleEnvironment() {
        BuildEnvironmentRest environmentRest = new BuildEnvironmentRest();
        environmentRest.setName("Test Environment");
        environmentRest.setBuildType(BuildType.NATIVE);
        return environmentRest;
    }
}
