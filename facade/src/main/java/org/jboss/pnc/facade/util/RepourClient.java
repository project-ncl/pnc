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
package org.jboss.pnc.facade.util;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.jboss.pnc.api.repour.dto.TranslateRequest;
import org.jboss.pnc.api.repour.dto.TranslateResponse;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.coordinator.maintenance.BlacklistAsyncInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbrazdil
 */
@Dependent
public class RepourClient {

    private Logger logger = LoggerFactory.getLogger(RepourClient.class);

    private static final String TRANSLATE_ENDPOINT = "/git-external-to-internal";

    @Inject
    private GlobalModuleGroup globalModuleGroupConfiguration;

    private final Client client = ClientBuilder.newClient();

    public String translateExternalUrl(String externalUrl) {
        try {
            TranslateRequest request = new TranslateRequest(externalUrl);
            TranslateResponse response = client.target(globalModuleGroupConfiguration.getRepourUrl())
                    .path(TRANSLATE_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), TranslateResponse.class);
            if (StringUtils.isEmpty(response.getInternalUrl())) {
                throw new RuntimeException("Failed to read translation of external URL to internal one.");
            }
            return response.getInternalUrl();
        } catch (ProcessingException ex) {
            throw new RuntimeException("Failed to translate external URL to internal one.", ex);
        }
    }

}
