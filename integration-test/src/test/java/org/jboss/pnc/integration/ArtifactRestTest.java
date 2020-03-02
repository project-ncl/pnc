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
import io.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.bpm.model.ArtifactRepositoryRest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.ArtifactRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.ArtifactEndpoint;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

/**
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ArtifactRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String ARTIFACT_REST_SPECIFIC_ENDPOINT = "/pnc-rest/rest/artifacts/%d";

    private static AtomicBoolean isInitialized = new AtomicBoolean();

    private static ArtifactRest artifactRest1;
    private static ArtifactRest artifactRest2;
    private static ArtifactRest artifactRest3;

    private static ArtifactRestClient artifactRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, REST_WAR_PATH);
        restWar.addClass(ArtifactRestTest.class);
        restWar.addClass(ArtifactProvider.class);
        restWar.addClass(ArtifactEndpoint.class);
        restWar.addClass(ArtifactRestClient.class);
        restWar.addClass(ArtifactRepositoryRest.class);
        restWar.addClass(ArtifactRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if (artifactRestClient == null) {
            artifactRestClient = new ArtifactRestClient();
        }
        if (!isInitialized.getAndSet(true)) {
            List<ArtifactRest> arts = artifactRestClient.all().getValue();
            logger.debug(arts.toString());
            // look at org.jboss.pnc.demo.data.DatabaseDataInitializer builtArtifacts 1,2,3
            // artifact number 3 is 5th initialized
            artifactRest1 = arts.get(0);
            artifactRest2 = arts.get(1);
            artifactRest3 = arts.get(6);
        }
    }

    @Test
    public void testGetAllArtifacts() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueNotNullOrEmpty("content[0]");
    }

    @Test
    public void testGetAllArfifactsWithMd5() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("md5", artifactRest1.getMd5())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        // artifacts 1 and 2 have same MD5
        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(2).allSatisfy(map -> {
            assertThat(map).containsKey("id");
            assertThat(map.get("id")).isIn(artifactRest1.getId(), artifactRest2.getId());
        });
    }

    @Test
    public void testGetAllArfifactsWithSha1() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("sha1", artifactRest2.getSha1())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        // artifacts 2 and 3 have same SHA1
        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(2).allSatisfy(map -> {
            assertThat(map).containsKey("id");
            assertThat(map.get("id")).isIn(artifactRest2.getId(), artifactRest3.getId());
        });
    }

    @Test
    public void testGetAllArfifactsWithSha256() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("sha256", artifactRest1.getSha256())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        // artifacts 1 and 3 have same SHA256
        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(2).allSatisfy(map -> {
            assertThat(map).containsKey("id");
            assertThat(map.get("id")).isIn(artifactRest1.getId(), artifactRest3.getId());
        });
    }

    @Test
    public void testGetAllArfifactsWithMd5AndSha1() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("sha1", artifactRest2.getSha1())
                .queryParam("md5", artifactRest2.getMd5())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(1);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content[0].id", artifactRest2.getId());
    }

    @Test
    public void testGetAllArfifactsWithMd5AndSha256() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("sha256", artifactRest1.getSha256())
                .queryParam("md5", artifactRest1.getMd5())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(1);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content[0].id", artifactRest1.getId());
    }

    @Test
    public void testGetAllArfifactsWithSha1AndSha256() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .queryParam("sha256", artifactRest3.getSha256())
                .queryParam("sha1", artifactRest3.getSha1())
                .get(ArtifactRestClient.ARTIFACT_REST_ENDPOINT);
        ResponseAssertion.assertThat(response).hasStatus(200);

        List<Map<String, Object>> list = response.jsonPath().getList("content");
        assertThat(list).hasSize(1);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content[0].id", artifactRest3.getId());
    }

    @Test
    public void testGetSpecificArtifact() {
        Response response = given().headers(testHeaders)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(String.format(ARTIFACT_REST_SPECIFIC_ENDPOINT, artifactRest1.getId()));

        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("content.id", artifactRest1.getId());

    }
}
