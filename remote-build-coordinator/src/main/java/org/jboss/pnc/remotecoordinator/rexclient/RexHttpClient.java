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
package org.jboss.pnc.remotecoordinator.rexclient;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.remotecoordinator.rexclient.provider.ConflictResponseMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.BadRequestMapper;
import org.jboss.pnc.remotecoordinator.rexclient.provider.TaskNotFoundMapper;
import org.jboss.pnc.rex.api.TaskEndpoint;

import javax.ws.rs.Path;

@Path("/rest/tasks")
@RegisterRestClient(configKey = "scheduler-client")
@RegisterClientHeaders(MyHeaderPropagator.class)
@RegisterProviders({ @RegisterProvider(ConflictResponseMapper.class), @RegisterProvider(BadRequestMapper.class),
        @RegisterProvider(TaskNotFoundMapper.class) })
public interface RexHttpClient extends TaskEndpoint {
}