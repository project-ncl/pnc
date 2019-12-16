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

import io.restassured.http.ContentType;
import org.hamcrest.core.IsEqual;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;

import static io.restassured.RestAssured.given;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RestResponseFormattingTest extends AbstractTest {
    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addAsWebInfResource("beans-use-mock-remote-clients.xml", "beans.xml");

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        addBuildExecutorMock(enterpriseArchive);

        logger.info(enterpriseArchive.toString(true));
        logger.info(restWar.toString(true));

        return enterpriseArchive;
    }

    @Test
    public void shouldReturnErrorInJsonFormat() {
        String response = given().header("Accept", "application/json").contentType(ContentType.JSON).port(getHttpPort()).expect()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("errorMessage", IsEqual.equalTo("Test exception."))
                .when().get("/pnc-rest/rest/test/throw").asString();
        logger.info(response);

    }

    @Test
    public void shouldReturnInJsonAndStatus404() {
        String response = given().header("Accept", "application/json").contentType(ContentType.JSON).port(getHttpPort()).expect()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("errorType", IsEqual.equalTo("NotFoundException"))
                .when().get("/pnc-rest/rest/test/does-not-exists").asString();
        logger.info(response);
    }

    @Test
    public void shouldReturnInJsonAndStatusNoContent() {
        String response = given().header("Accept", "application/json").contentType(ContentType.JSON).port(getHttpPort()).expect()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode())
                .when().get("/pnc-rest/rest/test/nocontent").asString();
        logger.info(response);
    }

    @Test
    @Ignore //TODO enable once we updated to EAP7. Test fails, the problem seems to be in the backend
    public void shouldReturnStatusUnauthorizedAndHeader() {
        String response = given().header("Accept", "application/json").contentType(ContentType.JSON).port(getHttpPort()).expect()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .header("WWW-Authenticate", IsEqual.equalTo("Bearer realm=\"test\""))
                .when().get("/pnc-rest/rest/test/unathorized").asString();
        logger.info(response);
    }

}
