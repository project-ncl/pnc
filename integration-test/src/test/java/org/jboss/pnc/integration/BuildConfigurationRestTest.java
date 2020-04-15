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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.EnvironmentRestClient;
import org.jboss.pnc.integration.client.ProjectRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.utils.EndpointAuthenticationProvider;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String CLONE_PREFIX_DATE_FORMAT = "yyyyMMddHHmmss";

    public static final String VALID_EXTERNAL_REPO = "https://github.com/project-ncl/pnc1.git";
    public static final String VALID_INTERNAL_REPO = "ssh://git@github.com:22/project-ncl/pnc-local.git";

    private static int productId;
    private static int projectId;
    private static int configurationId;
    private static int environmentId;
    private static int repositoryConfigurationId;
    private static int repositoryConfiguration2Id;

    private static AtomicBoolean isInitialized = new AtomicBoolean();

    private static ProjectRestClient projectRestClient;
    private static EnvironmentRestClient environmentRestClient;
    private static BuildConfigurationRestClient buildConfigurationRestClient;
    private static UserRestClient userRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(BuildConfigurationProvider.class);
        restWar.addClass(BuildConfigurationEndpoint.class);
        restWar.addClass(BuildConfigurationRest.class);
        restWar.addClass(EndpointAuthenticationProvider.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @SuppressWarnings("unchecked")
    @Before
    public void prepareData() throws Exception {
        if (!isInitialized.getAndSet(true)) {
            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(CONFIGURATION_REST_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    FIRST_CONTENT_ID,
                                    value -> configurationId = Integer.valueOf(value)));

            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(PRODUCT_REST_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    FIRST_CONTENT_ID,
                                    value -> productId = Integer.valueOf(value)));

            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(String.format(EnvironmentRestClient.ENVIRONMENT_REST_ENDPOINT, productId))
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    FIRST_CONTENT_ID,
                                    value -> environmentId = Integer.valueOf(value)));

            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(PROJECT_REST_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    FIRST_CONTENT_ID,
                                    value -> projectId = Integer.valueOf(value)));

            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(REPOSITORY_CONFIGURATION_REST_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    FIRST_CONTENT_ID,
                                    value -> repositoryConfigurationId = Integer.valueOf(value)));

            given().headers(testHeaders)
                    .contentType(ContentType.JSON)
                    .port(getHttpPort())
                    .when()
                    .get(REPOSITORY_CONFIGURATION_REST_ENDPOINT)
                    .then()
                    .statusCode(200)
                    .body(
                            JsonMatcher.containsJsonAttribute(
                                    "content[1].id",
                                    value -> repositoryConfiguration2Id = Integer.valueOf(value)));
        }

        if (projectRestClient == null) {
            projectRestClient = new ProjectRestClient();
        }
        if (environmentRestClient == null) {
            environmentRestClient = new EnvironmentRestClient();
        }
        if (buildConfigurationRestClient == null) {
            buildConfigurationRestClient = new BuildConfigurationRestClient();
        }
        if (userRestClient == null) {
            userRestClient = new UserRestClient();
            userRestClient.createUser("admin");
            userRestClient.createUser("user");
        }
        new UserRestClient().getLoggedUser();
    }

    @Test
    @Ignore // TODO move to RepositoryConfigurationTest
    public void shouldFailToCreateBuildConfigurationWhichDoesntMatchRegexp() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_create_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", ":");

        given().headers(testHeaders)
                .body(configurationTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(CONFIGURATION_REST_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    @Ignore // TODO move to RepositoryConfigurationTest
    public void shouldNotCreateWithInternalUrlNotMatchingPattern() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_WithEmptyCreateDate_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());
        configurationTemplate.addValue("_scmRepoUrl", VALID_EXTERNAL_REPO);

        Response response = given().headers(testHeaders)
                .body(configurationTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(CONFIGURATION_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(400);
    }

    @Test
    @InSequence(3)
    public void shouldChangeRepositoryConfiguration() {
        // given
        BuildConfigurationRest buildConfigurationRest = buildConfigurationRestClient.get(configurationId).getValue();
        // make sure this RC is not already set
        Assert.assertNotEquals(
                buildConfigurationRest.getRepositoryConfiguration().getId().intValue(),
                repositoryConfiguration2Id);

        // when
        RepositoryConfigurationRest repositoryConfigurationRest = RepositoryConfigurationRest.builder()
                .id(repositoryConfiguration2Id)
                .build();
        buildConfigurationRest.setRepositoryConfiguration(repositoryConfigurationRest);
        buildConfigurationRestClient.update(configurationId, buildConfigurationRest);

        // then
        BuildConfigurationRest buildConfigurationRestUpdated = buildConfigurationRestClient.get(configurationId)
                .getValue();
        assertEquals(
                repositoryConfiguration2Id,
                buildConfigurationRestUpdated.getRepositoryConfiguration().getId().intValue());
    }

    @Test
    public void shouldFailToCreateNewBuildConfigurationBecauseIdIsNotNull() throws IOException {
        // given
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_with_id_template");
        configurationTemplate.addValue("_id", String.valueOf(Integer.MAX_VALUE));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositoryConfigurationId", String.valueOf(repositoryConfigurationId));

        given().headers(testHeaders)
                .body(configurationTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(CONFIGURATION_REST_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldGetConflictWhenCreatingNewBuildConfigurationWithTheSameNameAndProjectId() throws IOException {
        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder
                .fromResource("buildConfiguration_WithEmptyCreateDate_template");
        configurationTemplate.addValue("_projectId", String.valueOf(projectId));
        configurationTemplate.addValue("_environmentId", String.valueOf(environmentId));
        configurationTemplate.addValue("_name", UUID.randomUUID().toString());
        configurationTemplate.addValue("_repositoryConfigurationId", String.valueOf(repositoryConfigurationId));

        Response firstAttempt = given().headers(testHeaders)
                .body(configurationTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(CONFIGURATION_REST_ENDPOINT);

        Response secondAttempt = given().headers(testHeaders)
                .body(configurationTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(CONFIGURATION_REST_ENDPOINT);

        ResponseAssertion.assertThat(firstAttempt).hasStatus(201);
        ResponseAssertion.assertThat(secondAttempt).hasStatus(409);
    }

    // TODO Test will fail due to issue: NCL-4473, remove @Ignore when fixed.
    @Ignore
    @Test
    public void shouldReturn404WhenRevisionToRestoreDoesNotExist() throws Exception {
        // given
        int rev = 17389; // Probably doesn't exist.

        // when
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(
                        String.format(
                                CONFIGURATION_SPECIFIC_REST_ENDPOINT + "/revisions/%d/restore",
                                configurationId,
                                rev));

        // then
        ResponseAssertion.assertThat(response).hasStatus(Status.NOT_FOUND.getStatusCode());
    }

}
