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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.RepositoryConfigurationEndpoint;
import org.jboss.pnc.rest.provider.AbstractProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static io.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.junit.Assert.assertEquals;

/**
 * @author Jakub Bartecek
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RepositoryConfigurationRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String VALID_EXTERNAL_REPO = "https://github.com/project-ncl/pnc1.git";

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
        return enterpriseArchive;
    }

    private int processCreateRequest(
            String templateName,
            int expectedReponseCode,
            String internalUrl,
            String externalUrl) throws IOException {
        JsonTemplateBuilder requestTemplate = JsonTemplateBuilder.fromResource(templateName);
        if (internalUrl != null)
            requestTemplate.addValue("_internalUrl", internalUrl);
        if (externalUrl != null)
            requestTemplate.addValue("_externalUrl", externalUrl);

        Response response = given().headers(testHeaders)
                .body(requestTemplate.fillTemplate())
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .post(REPOSITORY_CONFIGURATION_REST_ENDPOINT);
        assertEquals(expectedReponseCode, response.getStatusCode());
        if (expectedReponseCode >= 200 && expectedReponseCode < 300)
            return response.jsonPath().get(CONTENT_ID);
        else
            return 0;
    }

    @Test
    public void shouldFailToCreateNewWithInternalUrlNotMatchingPattern() throws IOException {
        processCreateRequest("repositoryConfiguration_create_template", 400, VALID_EXTERNAL_REPO, null);
    }

}
