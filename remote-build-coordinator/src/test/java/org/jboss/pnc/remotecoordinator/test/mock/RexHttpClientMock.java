/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.remotecoordinator.rexclient.RexHttpClient;
import org.jboss.pnc.rex.api.parameters.TaskFilterParameters;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;

@ApplicationScoped
@Alternative
@RestClient
public class RexHttpClientMock implements RexHttpClient {
    @Override
    public Set<TaskDTO> start(@Valid @NotNull CreateGraphRequest request) {
        return Collections.emptySet();
    }

    @Override
    public Set<TaskDTO> getAll(TaskFilterParameters filterParameters, List<String> queueFilter) {
        return Collections.emptySet();
    }

    @Override
    public TaskDTO getSpecific(@NotBlank String taskID) {
        return null;
    }

    @Override
    public Response cancel(@NotBlank String taskID) {
        return null;
    }

    @Override
    public Set<TaskDTO> byCorrelation(String correlationID) {
        return Collections.emptySet();
    }
}
