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
package org.jboss.pnc.causewayclient;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jboss.pnc.api.causeway.dto.push.BuildImportRequest;
import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class DefaultCausewayClient implements CausewayClient {

    Logger logger = LoggerFactory.getLogger(DefaultCausewayClient.class);

    private String untagEndpoint;

    @Deprecated // CDI workaround
    public DefaultCausewayClient() {
    }

    @Inject
    public DefaultCausewayClient(GlobalModuleGroup globalConfig) {
        String causewayBaseUrl = globalConfig.getExternalCausewayUrl();
        untagEndpoint = causewayBaseUrl + "/untag";
    }

    boolean post(String url, String jsonMessage, String authToken) {
        Header authHeader = new BasicHeader("Authorization", "Bearer " + authToken);
        HttpResponse response;
        try {
            logger.info("Making POST request to {}.", url);
            if (logger.isDebugEnabled()) {
                logger.debug("Request body {}.", secureBodyLog(jsonMessage));
            }

            Request request = Request.Post(url)
                    .addHeader(authHeader)
                    .bodyString(jsonMessage, ContentType.APPLICATION_JSON);
            MDCUtils.getHeadersFromMDC().forEach(request::addHeader);
            response = request.execute().returnResponse();
        } catch (IOException e) {
            logger.error("Failed to invoke remote Causeway.", e);
            return false;
        }
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("Response status: {}", statusCode);
            logger.debug("Response: " + EntityUtils.toString(response.getEntity()));
            if (!HttpUtils.isSuccess(statusCode)) {
                return false;
            }
        } catch (IOException e) {
            logger.error("Failed to read Causeway response.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean untagBuild(UntagRequest untagRequest, String authToken) {
        String jsonMessage = JsonOutputConverterMapper.apply(untagRequest);
        return post(untagEndpoint, jsonMessage, authToken);
    }

    /**
     * Makes the request body secure - removes any tokens
     *
     * @param jsonMessage Original body message
     * @return JSON string without token information
     */
    String secureBodyLog(String jsonMessage) {
        return jsonMessage.replaceAll("Bearer \\p{Print}+?\"", "Bearer ***\"");
    }

}
