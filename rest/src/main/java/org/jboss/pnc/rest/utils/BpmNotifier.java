/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.utils; //TODO move out of utils

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.rest.notifications.websockets.JSonOutputConverter;
import org.jboss.pnc.spi.BuildResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmNotifier { //TODO rename: remove bpm for name

    private final Logger log = Logger.getLogger(BpmNotifier.class);
    private BpmModuleConfig bpmConfig;

    @Deprecated
    public BpmNotifier() { //CDI workaround
    }

    @Inject
    public BpmNotifier(Configuration configuration) throws ConfigurationParseException {
        bpmConfig = configuration.getModuleConfig(new PncConfigProvider<BpmModuleConfig>(BpmModuleConfig.class));
    }

    public void sendBuildExecutionCompleted(String uri, BuildResult buildResult) {
        HttpPost request = new HttpPost(uri);

        List<NameValuePair> urlParameters = new ArrayList<>();

        JSonOutputConverter converter = new JSonOutputConverter();
        urlParameters.add(new BasicNameValuePair("buildResult", converter.apply(buildResult)));

        try {
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred preparing callback request.", e);
        }

        request.addHeader("Authorization", getAuthHeader());
        log.info("Executing request " + request.getRequestLine());

        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                log.info(response.getStatusLine());
            }
        } catch (IOException e) {
            log.error("Error occurred executing the callback.", e);
        }
    }

    private String getAuthHeader() {
        byte[] encodedBytes = Base64.encodeBase64((bpmConfig.getUsername() + ":" + bpmConfig.getPassword()).getBytes());
        return "Basic " + new String(encodedBytes);
    }

    public void simpleHttpPostCallback(String uri) {
        HttpPost request = new HttpPost(uri);
        request.addHeader("Authorization", getAuthHeader());
        log.info("Executing request " + request.getRequestLine());

        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                log.info(response.getStatusLine());
            }
        } catch (IOException e) {
            log.error("Error occurred executing the callback.", e);
        }
    }
}
