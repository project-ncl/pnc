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
package org.jboss.pnc.integration.mock.client;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.dingroguclient.DingroguBrewPushDTO;
import org.jboss.pnc.dingroguclient.DingroguBuildPushDTO;
import org.jboss.pnc.dingroguclient.DingroguBuildWorkDTO;
import org.jboss.pnc.dingroguclient.DingroguClient;
import org.jboss.pnc.dingroguclient.DingroguDeliverablesAnalysisDTO;
import org.jboss.pnc.dingroguclient.DingroguRepositoryCreationDTO;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

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
