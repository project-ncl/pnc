/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.pnc.bpm.model.BuildDriverResultRest;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.setup.Credentials;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
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
import java.net.URL;

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
    public void shouldAcceptCompletionResultAsSingleJson() throws RemoteResourceException {
        // given
        BuildDriverResult buildDriverResult = BuildDriverResultMock.mockResult(BuildStatus.SYSTEM_ERROR);
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
