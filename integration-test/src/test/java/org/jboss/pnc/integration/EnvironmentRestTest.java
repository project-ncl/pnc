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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.AbstractRestClient;
import org.jboss.pnc.integration.client.EnvironmentRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.rest.endpoint.BuildEnvironmentEndpoint;
import org.jboss.pnc.rest.provider.BuildEnvironmentProvider;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.jboss.pnc.test.util.JsonUtils.toJson;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class EnvironmentRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer environmentId;
    
    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(BuildEnvironmentProvider.class);
        restWar.addClass(BuildEnvironmentEndpoint.class);
        restWar.addClass(BuildEnvironmentRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Ignore("Ignored as the creation of environment is disabled until we'll implement authorization")
    @Test
    @InSequence(0)
    public void shouldCreateNewEnvironment() throws Exception {
        //given
        String environment = toJson(exampleEnvironment());

        //when
        Response response = given()
                .headers(testHeaders)
                .body(environment).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(EnvironmentRestClient.ENVIRONMENT_REST_ENDPOINT);
        environmentId = ResponseUtils.getIdFromLocationHeader(response);

        //then
        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/environments\\/\\d+");
        assertThat(environmentId).isNotNull();
    }

    @Ignore("Ignored as the creation of environment is disabled until we'll implement authorization")
    @Test
    @InSequence(1)
    public void shouldUpdateEnvironment() throws Exception {
        //given
        BuildEnvironmentRest environmentModified = exampleEnvironment();
        environmentModified.setSystemImageType(SystemImageType.DOCKER_IMAGE);

        //when
        Response putResponse = given()
                .headers(testHeaders)
                .body(toJson(environmentModified)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        Response getResponse = given()
                .headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        BuildEnvironmentRest noLoremIpsum = getResponse.jsonPath().getObject(AbstractRestClient.CONTENT, BuildEnvironmentRest.class);

        //then
        ResponseAssertion.assertThat(putResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(200);
        assertThat(noLoremIpsum.getSystemImageType()).isEqualTo(SystemImageType.DOCKER_IMAGE);
    }

    @Ignore("Ignored as the creation of environment is disabled until we'll implement authorization")
    @Test
    @InSequence(2)
    public void shouldDeleteEnvironment() throws Exception {
        //when
        Response deleteResponse = given()
                .headers(testHeaders).port(getHttpPort()).when()
                .delete(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        Response getResponse = given()
                .headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(SPECIFIC_ENVIRONMENT_REST_ENDPOINT, environmentId));

        //then
        ResponseAssertion.assertThat(deleteResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(404);
    }

    private BuildEnvironmentRest exampleEnvironment() {
        BuildEnvironmentRest environmentRest = new BuildEnvironmentRest();
        environmentRest.setName("Test Environment");
        environmentRest.setSystemImageType(SystemImageType.DOCKER_IMAGE);
        environmentRest.setSystemImageId("abcd1234");
        return environmentRest;
    }
}
