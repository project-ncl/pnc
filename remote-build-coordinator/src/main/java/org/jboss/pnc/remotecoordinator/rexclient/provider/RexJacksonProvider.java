/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.provider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_PATCH_JSON })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_PATCH_JSON })
public class RexJacksonProvider implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return JsonOutputConverterMapper.getMapper();
    }
}
