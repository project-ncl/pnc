/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient;

import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.remotecoordinator.rexclient.provider.BadRequestMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.ConflictResponseMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.LoggingFilter;
import org.jboss.pnc.remotecoordinator.rexclient.provider.NotFoundMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.RexJacksonProvider;
import org.jboss.pnc.rex.api.TaskEndpoint;

@Path("/rest/tasks")
@RegisterRestClient(configKey = "scheduler-client")
@RegisterClientHeaders(MyHeaderPropagator.class)
@RegisterProviders({
        @RegisterProvider(ConflictResponseMapper.class),
        @RegisterProvider(BadRequestMapper.class),
        @RegisterProvider(NotFoundMapper.class),
        @RegisterProvider(LoggingFilter.class),
        @RegisterProvider(RexJacksonProvider.class) })
public interface RexHttpClient extends TaskEndpoint {
}