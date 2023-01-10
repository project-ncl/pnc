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
package org.jboss.pnc.remotecoordinator.test.mock;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.remotecoordinator.rexclient.RexHttpClient;
import org.jboss.pnc.rex.api.parameters.TaskFilterParameters;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@ApplicationScoped
@Alternative
@RestClient
public class RexHttpClientMock implements RexHttpClient {
    @Override
    public Set<TaskDTO> start(@Valid @NotNull CreateGraphRequest request) {
        return Collections.emptySet();
    }

    @Override
    public Set<TaskDTO> getAll(TaskFilterParameters filterParameters) {
        return Collections.emptySet();
    }

    @Override
    public TaskDTO getSpecific(@NotBlank String taskID) {
        return null;
    }

    @Override
    public void cancel(@NotBlank String taskID) {
    }

    @Override
    public Set<TaskDTO> byCorrelation(String correlationID) {
        return Collections.emptySet();
    }
}
