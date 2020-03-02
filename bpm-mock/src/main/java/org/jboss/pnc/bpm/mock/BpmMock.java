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
package org.jboss.pnc.bpm.mock;

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.exception.CoreException;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/25/16 Time: 3:29 PM
 */
@SuppressWarnings("WeakerAccess")
public class BpmMock extends BpmManager {

    private MockKieSession session = new MockKieSession();
    private Optional<Consumer<BpmTask>> onTaskStarted = Optional.empty();

    public BpmMock() throws ConfigurationParseException, CoreException {
        super(mockBpmConfig());
        super.init();
    }

    @Override
    protected KieSession initKieSession() throws CoreException {
        mockBpmConfig();
        session.onStartProcess(this::startProcessMock);
        return session;
    }

    public boolean startTask(BpmTask task) throws CoreException {
        boolean started = super.startTask(task);
        onTaskStarted.ifPresent(supplier -> supplier.accept(task));
        return started;
    }

    public void setOnTaskStarted(Consumer<BpmTask> onTaskStarted) {
        this.onTaskStarted = Optional.of(onTaskStarted);
    }

    private static BpmModuleConfig mockBpmConfig() {
        BpmModuleConfig bpmConfig = Mockito.mock(BpmModuleConfig.class);
        Mockito.when(bpmConfig.getMilestoneReleaseProcessId()).thenReturn("1.1.1");
        return bpmConfig;
    }

    protected ProcessInstance startProcessMock(String processName, Map params) {
        return new ProcessInstanceMock();
    }

    @PostConstruct
    public void setUp() throws CoreException {
        super.init();
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
