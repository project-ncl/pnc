/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.pnc.api.reqour.dto.TranslateRequest;
import org.jboss.pnc.api.reqour.dto.TranslateResponse;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Dependent
@Slf4j
public class ReqourClient {

    private static final String TRANSLATE_ENDPOINT = "/external-to-internal";

    @Inject
    private GlobalModuleGroup globalModuleGroupConfiguration;

    private final Client client = ClientBuilder.newClient();

    public String translateExternalUrl(String externalUrl) {
        try {
            TranslateRequest request = TranslateRequest.builder().externalUrl(externalUrl).build();
            TranslateResponse response = client.target(globalModuleGroupConfiguration.getExternalReqourUrl())
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
