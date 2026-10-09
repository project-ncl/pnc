/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock.client;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.dingroguclient.DingroguBuildPushDTO;
import org.jboss.pnc.dingroguclient.DingroguBuildWorkDTO;
import org.jboss.pnc.dingroguclient.DingroguClient;
import org.jboss.pnc.dingroguclient.DingroguDeliverablesAnalysisDTO;
import org.jboss.pnc.dingroguclient.DingroguRepositoryCreationDTO;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;

@ApplicationScoped
public class DingroguClientMock implements DingroguClient {

    @Override
    public Request startBuildProcessInstance(
            RemoteBuildTask buildTask,
            List<Request.Header> headers,
            String correlationId) {
        return null;
    }

    @Override
    public void submitDeliverablesAnalysis(DingroguDeliverablesAnalysisDTO dto) {

    }

    @Override
    public void submitBuildPush(DingroguBuildPushDTO dto) {

    }

    @Override
    public void submitRepositoryCreation(DingroguRepositoryCreationDTO dto) {

    }

    @Override
    public Request cancelProcessInstance(List<Request.Header> headers, String correlationId) {
        return null;
    }

    @Override
    public void submitCancelProcessInstance(String correlationId) {

    }

    @Override
    public DingroguBuildWorkDTO createDTO(RemoteBuildTask buildTask, String correlationId) {
        return null;
    }
}
