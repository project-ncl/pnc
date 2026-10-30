/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

@Provider
@Consumes({ MediaType.APPLICATION_JSON, "application/*+json" })
@Produces({ MediaType.APPLICATION_JSON, "application/*+json" })
public class RepoDriverClientJacksonProvider extends ResteasyJackson2Provider {

    public RepoDriverClientJacksonProvider() {
        super();
        setMapper(JsonOutputConverterMapper.getMapper());
    }
}
