/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.utils.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.ConfigProvider;
import org.jboss.pnc.rest.restmodel.bpm.MilestoneReleaseParameters;
import org.jboss.pnc.rest.restmodel.causeway.MilestoneReleaseRest;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.test.util.JsonUtils;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/25/16
 * Time: 3:29 PM
 */
@SuppressWarnings("WeakerAccess")
public class BpmMock extends BpmManager {

    private final List<Push> pushes = new ArrayList<>();
    private MockKieSession session = new MockKieSession();

    public BpmMock() throws ConfigurationParseException {
        super(mockConfiguration());
    }

    private static Configuration mockConfiguration() throws ConfigurationParseException {
        BpmModuleConfig bpmConfig = mockBpmConfig();
        Configuration configuration = mock(Configuration.class);
        when(configuration.getModuleConfig(any(ConfigProvider.class))).thenReturn(bpmConfig);
        return configuration;
    }

    @Override
    protected KieSession initKieSession() throws CoreException {
        mockBpmConfig();
        pushes.clear();
        session.onStartProcess(this::startProcessMock);
        return session;
    }

    private static BpmModuleConfig mockBpmConfig() {
        BpmModuleConfig bpmConfig = mock(BpmModuleConfig.class);
        when(bpmConfig.getMilestoneReleaseProcessId()).thenReturn("1.1.1");
        return bpmConfig;
    }

    private ProcessInstance startProcessMock(String processName, Map params) {
        Integer taskId = (Integer) params.get("taskId");
        MilestoneReleaseRest milestoneRest;
        try {
            MilestoneReleaseParameters processParameters = JsonUtils
                    .fromJson((String) params.get("processParameters"), MilestoneReleaseParameters.class);
            milestoneRest = processParameters.getBrewPush();
        } catch (IOException e) {
            throw new RuntimeException("failed to read brew push milestone rest from json", e);
        }
        String callbackId = RandomStringUtils.randomNumeric(12);
        pushes.add(new Push(milestoneRest.getMilestoneId(), taskId, callbackId));
        return new ProcessInstanceMock();
    }

    public Response getPushesFor(int milestoneId) {
        List<Push> pushes = this.pushes.stream()
                .filter(p -> p.milestoneId == milestoneId)
                .collect(Collectors.toList());
        return Response.ok(new PushList(pushes)).build();
    }

    @PostConstruct
    public void setUp() throws CoreException {
        super.init();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PushList {
        List<Push> pushes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Push {
        private int milestoneId;
        private int taskId;
        private String callbackId;
    }

    public static class ProcessInstanceMock implements ProcessInstance {

        @Override
        public String getProcessId() {
            return null;
        }

        @Override
        public Process getProcess() {
            return null;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getProcessName() {
            return null;
        }

        @Override
        public int getState() {
            return 0;
        }

        @Override
        public long getParentProcessInstanceId() {
            return 0;
        }

        @Override
        public void signalEvent(String s, Object o) {
        }

        @Override
        public String[] getEventTypes() {
            return new String[0];
        }
    }

}
