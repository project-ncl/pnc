/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.endpoints;

import static io.restassured.RestAssured.given;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.BASE_REST_PATH;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordAliasEndpointTest {

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void testRedirect() {
        int buildRecordId = 100;
        given().redirects()
                .follow(false)
                .port(8080)
                .when()
                .get(String.format(BASE_REST_PATH + "/build-records/%d", buildRecordId))
                .then()
                .assertThat()
                .statusCode(301)
                .and()
                .header(
                        "Location",
                        String.format("http://localhost:8080" + BASE_REST_PATH + "/builds/%d", buildRecordId));
    }
}
