/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration;

import static io.restassured.RestAssured.given;
import static org.jboss.pnc.integration.setup.IntegrationTestEnv.getHttpPort;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.BASE_REST_PATH;

import javax.ws.rs.core.Response;

import org.hamcrest.core.IsEqual;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.http.ContentType;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RestResponseFormattingTest {

    private static final Logger logger = LoggerFactory.getLogger(RestResponseFormattingTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldReturnErrorInJsonFormat() {
        String response = given().header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .expect()
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("errorMessage", IsEqual.equalTo("Test exception."))
                .when()
                .get(BASE_REST_PATH + "/debug/throw")
                .asString();
        logger.info(response);
    }

    @Test
    public void shouldReturnEmptyBodyAndStatus404() {
        String response = given().header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .expect()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .when()
                .get(BASE_REST_PATH + "/does-not-exists")
                .asString();
        logger.info(response);
        org.junit.Assert.assertTrue(response.isEmpty());
    }

    @Test
    public void shouldReturnInJsonAndStatusNoContent() {
        String response = given().header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .expect()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode())
                .when()
                .get(BASE_REST_PATH + "/debug/nocontent")
                .asString();
        logger.info(response);
    }

    @Test
    public void shouldReturnStatusUnauthorizedAndHeader() {
        String response = given().header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .expect()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode())
                .header("WWW-Authenticate", IsEqual.equalTo("Basic realm=\"debug\""))
                .when()
                .get(BASE_REST_PATH + "/debug/unauthorized")
                .asString();
        logger.info("response: {}", response);
    }
}
