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
package org.jboss.pnc.integration_new.endpoint;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.getHttpClient;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BpmEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(BpmEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldNotifyTask() throws ClientProtocolException, IOException {

        int bpmTaskId = 1;

        Configuration config = RestClientConfiguration.asUser();
        String url = config.getProtocol() + "://" + config.getHost() + ":" + config.getPort() +
                "/pnc-rest-new/rest-new/bpm/tasks/" + bpmTaskId + "/notify";

        HttpPost theMethod = new HttpPost(url);
        String credentials = config.getBasicAuth().getUsername() + ":" + config.getBasicAuth().getPassword();
        theMethod.setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes())));
        theMethod.setHeader("Content-Type", "application/json");

        String data = "{\"eventType\":\"PROCESS_PROGRESS_UPDATE\",\"taskName\":\"PNC REST Task Repour Adjust\",\"bpmTaskStatus\":\"STARTED\"}";
        HttpEntity entity = new StringEntity(data, StandardCharsets.UTF_8);
        theMethod.setEntity(entity);

        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse response = httpclient.execute(theMethod);
        assertThat(response.getStatusLine().getStatusCode()).isBetween(200, 203);
    }
}
