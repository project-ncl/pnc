/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.rest.endpoint.BuildTaskEndpoint;
import org.jboss.pnc.rest.restmodel.BuildExecutionConfigurationRest;
import org.jboss.pnc.rest.trigger.BuildExecutorTriggerer;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
@RunAsClient
public class BuildTasksRestTest extends AbstractTest{

    @ArquillianResource
    URL url;

    private static Logger log = LoggerFactory.getLogger(BuildTasksRestTest.class);

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(BuildTaskEndpoint.class);
        war.addClass(BuildExecutorTriggerer.class);
        log.info("EAR: " + enterpriseArchive.toString(true));
        log.info("WAR: " + war.toString(true));

        addBuildExecutorMock(enterpriseArchive);

        return enterpriseArchive;
    }

    @Test
    public void shouldTriggerBuildExecution() {
        HttpPost request = new HttpPost(url + "/pnc-rest/rest/build-tasks/execute-build");
        request.addHeader(getAuthenticationHeaderApache());

        BuildExecutionConfiguration buildExecutionConfig = BuildExecutionConfiguration.build(
                1,
                "test-content-id",
                1,
                "mvn clean install",
                "jboss-modules",
                "scm-url",
                "master",
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
                null);

        BuildExecutionConfigurationRest buildExecutionConfigurationRest = new BuildExecutionConfigurationRest(buildExecutionConfig);

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair("buildExecutionConfiguration", buildExecutionConfigurationRest.toString()));

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
                Assert.assertEquals("Received error response code. Response: " + printEntity(response), 200, statusCode);
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
