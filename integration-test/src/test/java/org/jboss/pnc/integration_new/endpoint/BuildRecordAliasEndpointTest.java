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
package org.jboss.pnc.integration_new.endpoint;

import io.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

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
        final Response response = given().redirects()
                .follow(false)
                .port(8080)
                .when()
                .get(String.format("/pnc-rest-new/rest-new/build-records/%d", buildRecordId));
        ResponseAssertion.assertThat(response).hasStatus(301);
        assertThat(response.getHeader("Location")).isNotNull()
                .isEqualTo(String.format("http://localhost:8080/pnc-rest-new/rest-new/builds/%d", buildRecordId));
    }
}
