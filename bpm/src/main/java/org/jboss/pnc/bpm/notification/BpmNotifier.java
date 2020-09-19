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

package org.jboss.pnc.bpm.notification;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.pnc.bpm.ConnectorSelector.GENERIC_PARAMETER_KEY;
import static org.jboss.pnc.bpm.ConnectorSelector.RHPAM;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmNotifier {

    private static final Logger log = LoggerFactory.getLogger(BpmNotifier.class);
    private BpmModuleConfig bpmConfig;

    private BuildResultMapper mapper;

    @Deprecated
    public BpmNotifier() { // CDI workaround
    }

    @Inject
    public BpmNotifier(Configuration configuration, BuildResultMapper mapper) throws ConfigurationParseException {
        bpmConfig = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        this.mapper = mapper;
    }

    public void sendBuildExecutionCompleted(String uri, BuildResult buildResult, String accessToken) {
        log.debug("Preparing to send build result to BPM {}.", buildResult);
        BuildResultRest buildResultRest = null;
        String errMessage = "";
        try {
            buildResultRest = mapper.toDTO(buildResult);
            if (log.isTraceEnabled()) {
                log.trace("Sending build result to BPM {}.", buildResultRest.toFullLogString());
            } else {
                log.debug("Sending build result to BPM {}.", buildResultRest);
            }
        } catch (Throwable e) {
            log.error("Cannot construct rest result.", e);
            errMessage = "Cannot construct rest result: " + e.getMessage();
        }

        HttpPost request = new HttpPost(uri);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(
                new BasicNameValuePair(
                        "event",
                        buildResultRest != null ? buildResultRest.toFullLogString()
                                : "{\"error\", \"" + errMessage + "\"}"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8);
        if (entity != null) {
            request.setEntity(entity);
            log.debug("Response entity set to the post request for uri: " + uri);
        } else {
            log.error("Missing response entity to post to: " + uri);
        }

        if (buildResult.getBuildExecutionConfiguration().isPresent()) {
            BuildExecutionConfiguration buildExecutionConfiguration = buildResult.getBuildExecutionConfiguration()
                    .get();
            if (buildExecutionConfiguration.getGenericParameters()
                    .getOrDefault(GENERIC_PARAMETER_KEY, "")
                    .equals(RHPAM)) {
                configureRequestNewBPM(accessToken, request);
            } else {
                request.addHeader("Authorization", getAuthHeader());
            }
        } else {
            request.addHeader("Authorization", getAuthHeader());
        }

        // get id for logging
        String buildExecutionConfigurationId;
        if (buildResult.getBuildExecutionConfiguration().isPresent()) {
            BuildExecutionConfiguration buildExecutionConfiguration = buildResult.getBuildExecutionConfiguration()
                    .get();
            buildExecutionConfigurationId = buildExecutionConfiguration.getId() + "";
        } else {
            buildExecutionConfigurationId = "NO BuildExecutionConfiguration.";
        }

        log.info(
                "Sending buildResult of buildExecutionConfiguration.id " + buildExecutionConfigurationId + ": "
                        + request.getRequestLine());

        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                log.info(response.getStatusLine().toString());
                try {
                    if (response.getStatusLine().getStatusCode() != 200) {
                        InputStream content = response.getEntity().getContent();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(content, writer);
                        log.debug("Received message: " + writer.toString());
                    }
                } catch (Exception e) {
                    log.warn("Cannot write http response message to log.", e);
                }
            }
        } catch (IOException e) {
            log.error("Error occurred executing the callback.", e);
        }
    }

    private void configureRequestNewBPM(String accessToken, HttpPost request) {
        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private String getAuthHeader() {
        byte[] encodedBytes = Base64.encodeBase64((bpmConfig.getUsername() + ":" + bpmConfig.getPassword()).getBytes());
        return "Basic " + new String(encodedBytes);
    }
}
