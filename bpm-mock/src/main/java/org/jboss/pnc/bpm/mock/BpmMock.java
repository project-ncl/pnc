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
package org.jboss.pnc.bpm.mock;

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/25/16 Time: 3:29 PM
 */
@SuppressWarnings("WeakerAccess")
public class BpmMock extends BpmManager {

    private static final Logger log = LoggerFactory.getLogger(BpmMock.class);

    private MockKieSession session = new MockKieSession();
    private Optional<Consumer<BpmTask>> onTaskStarted = Optional.empty();

    public BpmMock() throws ConfigurationParseException, CoreException, IOException {
        super(mockGlobalConfig(), mockBpmConfig());
    }

    @Override
    public boolean startTask(BpmTask task) throws CoreException {
        Connector connector = Mockito.mock(Connector.class);
        try {
            Mockito.when(connector.startProcess(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1L);
        } catch (ProcessManagerException e) {
            log.error("Cannot mock start process.", e);
        }
        task.setConnector(connector);
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

    private static GlobalModuleGroup mockGlobalConfig() {
        GlobalModuleGroup bpmConfig = Mockito.mock(GlobalModuleGroup.class);
        Mockito.when(bpmConfig.getBpmUrl()).thenReturn("http://bpm.svc.cluster.local/business-central");
        Mockito.when(bpmConfig.getIndyUrl()).thenReturn("http://indy.svc.cluster.local/");
        return bpmConfig;
    }

    public void setUp() throws CoreException {
        super.init();
    }
}
