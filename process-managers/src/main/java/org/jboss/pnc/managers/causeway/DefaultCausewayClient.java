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
package org.jboss.pnc.managers.causeway;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
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

    private final String BR_PUSH_PATH = "/import/build";

    Logger logger = LoggerFactory.getLogger(DefaultCausewayClient.class);

    private String causewayEndpoint;

    @Inject
    public DefaultCausewayClient(Configuration configuration) {
        try {
            String causewayBaseUrl = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class))
                    .getCausewayBaseUrl();
            causewayEndpoint = causewayBaseUrl + BR_PUSH_PATH;
        } catch (ConfigurationParseException e) {
            logger.error("There is a problem while parsing system configuration. Using defaults.", e);
        }

    }

    @Override
    public boolean push(String jsonMessage, String authToken) {
        Header authHeader = new BasicHeader("Authorization", "Bearer " + authToken);

        try {
            HttpResponse response = Request.Post(causewayEndpoint)
                    .addHeader(authHeader)
                    .bodyString(jsonMessage, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnResponse();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Trying to invoke remote Causeway push failed with http code {}.", statusCode);
                return false;
            }

        } catch (IOException e) {
            logger.error("Trying to invoke remote Causeway push failed.", e);
            return false;
        }
        return true;
    }
}
