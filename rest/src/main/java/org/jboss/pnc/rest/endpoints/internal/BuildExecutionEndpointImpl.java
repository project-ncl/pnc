/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.buildagent.api.TaskStatusUpdateEvent;
import org.jboss.pnc.facade.executor.BuildExecutorTriggerer;
import org.jboss.pnc.rest.endpoints.internal.api.BuildExecutionEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class BuildExecutionEndpointImpl implements BuildExecutionEndpoint {

    @Inject
    private BuildExecutorTriggerer buildExecutorTriggerer;

    @Override
    public Response buildExecutionCompleted(TaskStatusUpdateEvent updateEvent) {
        buildExecutorTriggerer.buildStatusUpdated(updateEvent);
        return Response.ok().build();
    }
}
