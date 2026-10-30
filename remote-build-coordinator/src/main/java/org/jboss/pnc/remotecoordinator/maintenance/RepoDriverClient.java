/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.api.repositorydriver.rest.DriverEndpoint;
import org.jboss.pnc.remotecoordinator.rexclient.MyHeaderPropagator;
import org.jboss.pnc.remotecoordinator.rexclient.provider.BadRequestMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.RexJacksonProvider;

@ApplicationScoped
@RegisterRestClient(configKey = "repository-driver")
@RegisterClientHeaders(MyHeaderPropagator.class)
@RegisterProviders({
        @RegisterProvider(BadRequestMapper.class),
        @RegisterProvider(RexJacksonProvider.class) })
public interface RepoDriverClient extends DriverEndpoint {

}
