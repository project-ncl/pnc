/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.api.repositorydriver.rest.DriverEndpoint;
import org.jboss.pnc.remotecoordinator.rexclient.MyHeaderPropagator;

@ApplicationScoped
@RegisterRestClient(configKey = "repository-driver")
@RegisterClientHeaders(MyHeaderPropagator.class)
public interface RepoDriverClient extends DriverEndpoint {

}
