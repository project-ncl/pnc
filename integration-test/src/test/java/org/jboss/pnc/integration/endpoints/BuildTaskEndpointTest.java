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
package org.jboss.pnc.integration.endpoints;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.pnc.bpm.model.BuildDriverResultRest;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.integration.setup.Credentials;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.termdbuilddriver.DefaultBuildDriverResult;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jboss.pnc.integration.setup.RestClientConfiguration.BASE_REST_PATH;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildTaskEndpointTest {

    @ArquillianResource
    URL url;

    private static final Logger log = LoggerFactory.getLogger(BuildTaskEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldTriggerBuildExecution() throws RemoteResourceException {
        HttpPost request = new HttpPost(url + BASE_REST_PATH + "/build-tasks/execute-build");
        request.addHeader(Credentials.USER.createAuthHeader(BasicHeader::new));

        BuildExecutionConfiguration buildExecutionConfig = BuildExecutionConfiguration.build(
                "1",
                "test-content-id",
                "1",
                "mvn clean install",
                "12",
                "jboss-modules",
                "scm-url",
                "f18de64523d5054395d82e24d4e28473a05a3880",
                "1.0.0.redhat-1",
                "origin-scm-url",
                false,
                "dummy-docker-image-id",
                "dummy.repo.url/repo",
                SystemImageType.DOCKER_IMAGE,
                BuildType.MVN,
                false,
                null,
                new HashMap<>(),
                false,
                null,
                false,
                "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true");

        BuildExecutionConfigurationRest buildExecutionConfigurationRest = new BuildExecutionConfigurationRest(
                buildExecutionConfig);

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters
                .add(new BasicNameValuePair("buildExecutionConfiguration", buildExecutionConfigurationRest.toString()));

        try {
            request.setEntity(new UrlEncodedFormEntity(requestParameters));
        } catch (UnsupportedEncodingException e) {
            log.error("Cannot prepare request.", e);
            Assert.fail("Cannot prepare request." + e.getMessage());
        }

        log.info("Executing request {} with parameters: {}", request.getRequestLine(), requestParameters);

        int statusCode = -1;
        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(
                        "Received error response code. Response: " + printEntity(response),
                        200,
                        statusCode);
            }
        } catch (IOException e) {
            Assertions.fail("Cannot invoke remote endpoint.", e);
        }
    }

    @Test
    public void shouldAcceptCompletionResultAsSingleJson() throws RemoteResourceException {
        // given
        BuildDriverResult buildDriverResult = new DefaultBuildDriverResult(
                "The log!",
                BuildStatus.SYSTEM_ERROR,
                java.util.Optional.of("12345"));

        BuildDriverResultRest buildDriverResultRest = new BuildDriverResultRest(buildDriverResult);
        BuildResultRest buildResultRest = new BuildResultRest();
        buildResultRest.setBuildDriverResult(buildDriverResultRest);

        // when
        HttpPost request = new HttpPost(url + BASE_REST_PATH + "/build-tasks/42/completed");
        request.addHeader(Credentials.USER.createAuthHeader(BasicHeader::new));
        request.addHeader("Content-type", MediaType.APPLICATION_JSON);

        String jsonBody = JsonOutputConverterMapper.apply(buildResultRest);
        log.debug("Json body: {}.", jsonBody);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

        // then
        int statusCode = -1;
        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                statusCode = response.getStatusLine().getStatusCode();
                Assert.assertEquals(
                        "Received error response code. Response: " + printEntity(response),
                        400, // validation failure is expected; 500 when deserialization fails
                        statusCode);
            }
        } catch (IOException e) {
            Assertions.fail("Cannot invoke remote endpoint.", e);
        }
    }

    private String printEntity(CloseableHttpResponse response) throws IOException {
        InputStream stream = response.getEntity().getContent();
        return IOUtils.toString(stream);
    }
}
