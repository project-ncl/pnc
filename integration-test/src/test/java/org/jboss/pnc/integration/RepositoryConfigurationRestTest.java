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
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.RepositoryConfigurationEndpoint;
import org.jboss.pnc.rest.provider.AbstractProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.BuildConfigurationRestTest.VALID_EXTERNAL_REPO;
import static org.jboss.pnc.integration.BuildConfigurationRestTest.VALID_INTERNAL_REPO;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SEARCH_QUERY_PARAM;
import static org.junit.Assert.assertEquals;

/**
 * @author Jakub Bartecek
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RepositoryConfigurationRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String VALID_INTERNAL_REPO_2 = "ssh://git@github.com:22/project-ncl/pnc-cli.git";

    public static final String VALID_EXTERNAL_REPO_2 = "https://github.com/project-ncl/pnc-cli.git";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(RepositoryConfigurationEndpoint.class);
        restWar.addClass(RepositoryConfigurationRest.class);
        restWar.addClass(RepositoryConfigurationProvider.class);
        restWar.addClass(AbstractProvider.class);

        logger.info(enterpriseArchive.toString(true));
        return  enterpriseArchive;
    }

    private static int repositoryConfigurationId;

    @Before
    public void before() {
    }

    @Test
    public void shouldGetAll() {

    }

    @Test
    @InSequence(1)
    public void shouldCreateNewWithInternalUrl() throws IOException {
        repositoryConfigurationId = processCreateRequest("repositoryConfiguration_create_template", 201, VALID_INTERNAL_REPO, null);
    }

    @Test
    @InSequence(2)
    public void shouldFailOnCreatingNewWithConflictingInternalUrl() throws IOException {
        processCreateRequest("repositoryConfiguration_create_template", 409, VALID_INTERNAL_REPO, null);
    }

    private int processCreateRequest(String templateName, int expectedReponseCode,
                                      String internalUrl, String externalUrl) throws IOException {
        JsonTemplateBuilder requestTemplate = JsonTemplateBuilder.fromResource(templateName);
        if(internalUrl != null)
            requestTemplate.addValue("_internalUrl", internalUrl);
        if(externalUrl != null)
            requestTemplate.addValue("_externalUrl", externalUrl);

        Response response = given().headers(testHeaders)
                .body(requestTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort()).when()
                .post(REPOSITORY_CONFIGURATION_REST_ENDPOINT);
        assertEquals(expectedReponseCode, response.getStatusCode());
        if(expectedReponseCode >= 200 && expectedReponseCode < 300)
            return response.jsonPath().get(CONTENT_ID);
        else
            return 0;
    }

    @Test
    public void shouldFailToCreateNewWithoutInternalUrl() throws IOException {
        processCreateRequest("repositoryConfiguration_create_template_external_url", 400, null, VALID_EXTERNAL_REPO);
    }


    @Test
    public void shouldFailToCreateNewWithInternalUrlNotMatchingPattern() throws IOException {
        processCreateRequest("repositoryConfiguration_create_template", 400, VALID_EXTERNAL_REPO, null);
    }

    @InSequence(3)
    @Test
    public void shouldUpdate() throws IOException {
        //given
        JsonTemplateBuilder requestTemplate = JsonTemplateBuilder.fromResource("repositoryConfiguration_all_fields");
        requestTemplate.addValue("_internalUrl", VALID_INTERNAL_REPO);
        requestTemplate.addValue("_externalUrl", VALID_EXTERNAL_REPO);
        requestTemplate.addValue("_id", String.valueOf(repositoryConfigurationId));
        requestTemplate.addValue("_preBuildSyncEnabled", String.valueOf(true));

        //when
        given().headers(testHeaders)
                .body(requestTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort()).when()
                .put(String.format(REPOSITORY_CONFIGURATION_SPECIFIC_REST_ENDPOINT, repositoryConfigurationId))
                .then()
                .statusCode(200);

        //then
        Response responseAfterUpdate = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(REPOSITORY_CONFIGURATION_SPECIFIC_REST_ENDPOINT, repositoryConfigurationId));
        ResponseAssertion.assertThat(responseAfterUpdate).hasStatus(200);
        ResponseAssertion.assertThat(responseAfterUpdate)
                .hasJsonValueEqual(CONTENT_ID, repositoryConfigurationId)
                .hasJsonValueEqual("content.externalUrl", VALID_EXTERNAL_REPO)
                .hasJsonValueEqual("content.preBuildSyncEnabled", String.valueOf(true));
    }

    @Test
    public void shouldNotAllowUpdatingInternalUrl() throws IOException {
        //given
        final String validInternalUrl2 = "git+ssh://git-repo-user@git-repo.devvm.devcloud.example.com:12839/booValid2.git";
        JsonTemplateBuilder requestTemplate = JsonTemplateBuilder.fromResource("repositoryConfiguration_create_template");
        requestTemplate.addValue("_internalUrl", validInternalUrl2);

        //when
        given().headers(testHeaders)
                .body(requestTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort()).when()
                .put(String.format(REPOSITORY_CONFIGURATION_SPECIFIC_REST_ENDPOINT, repositoryConfigurationId))
                .then()
                .statusCode(400);
    }

    @Test
    @InSequence(4)
    public void shouldCreateNewWithBothUrls() throws IOException {
        repositoryConfigurationId = processCreateRequest("repositoryConfiguration_create_template_both_urls", 201, VALID_INTERNAL_REPO_2, VALID_EXTERNAL_REPO_2);
    }

    @InSequence(5)
    @Test
    public void shouldMatchFullExternalRepositoryUrl() throws IOException {
        // given record inserted in shouldCreateNewWithBothUrls VALID_INTERNAL_REPO_2, VALID_EXTERNAL_REPO_2
        final String requestUrl1 = "git+ssh://github.com/project-ncl/pnc-cli";
        final String requestUrl2 = "git+ssh://github.com/project-ncl/pnc-cli.git";
        final String requestUrl3 = "https://github.com/project-ncl/pnc-cli.git";

        matchFullExternalRepositoryUrl(requestUrl1, true);
        matchFullExternalRepositoryUrl(requestUrl2, true);
        matchFullExternalRepositoryUrl(requestUrl3, true);
    }

    @InSequence(6)
    @Test
    public void shouldNotMatchPartialExternalRepositoryUrl(){
        // given record inserted in shouldUpdate VALID_INTERNAL_REPO, VALID_EXTERNAL_REPO
        final String requestUrl1 = "https://github.com";
        final String requestUrl2 = "ssh://github.com/project-ncl";

        matchFullExternalRepositoryUrl(requestUrl1, false);
        matchFullExternalRepositoryUrl(requestUrl2, false);
    }

    private void matchFullExternalRepositoryUrl(String requestUrl1, boolean shouldSucceed) {
        Response responseAll = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(REPOSITORY_CONFIGURATION_REST_ENDPOINT);
        System.out.println(responseAll.asString());

        //when
        Response responseMatch = given().headers(testHeaders)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .queryParam(SEARCH_QUERY_PARAM, requestUrl1)
                .get(REPOSITORY_CONFIGURATION_MATCH_REST_ENDPOINT);

        // then
        if(shouldSucceed) {
            ResponseAssertion.assertThat(responseMatch).hasStatus(200);
            List<RepositoryConfigurationRest> returnedObjects = responseMatch.path("content.externalUrl");
            assertEquals(1, returnedObjects.size());
            ResponseAssertion.assertThat(responseMatch)
                    .hasJsonValueEqual("content[0].externalUrl", VALID_EXTERNAL_REPO_2);
        }
        else {
            ResponseAssertion.assertThat(responseMatch).hasStatus(204);
        }
    }
}
