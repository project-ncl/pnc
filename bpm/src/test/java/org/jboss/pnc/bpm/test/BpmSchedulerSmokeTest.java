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

package org.jboss.pnc.bpm.test;

import lombok.Getter;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.KieClientConnector;
import org.jboss.pnc.bpm.model.BpmStringMapNotificationRest;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.pnc.bpm.BpmEventType.RC_REPO_CREATION_ERROR;
import static org.jboss.pnc.bpm.BpmEventType.RC_REPO_CREATION_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jakub Senko
 */
@RunWith(MockitoJUnitRunner.class)
public class BpmSchedulerSmokeTest {

    private static final Logger LOG = LoggerFactory.getLogger(BpmSchedulerSmokeTest.class);

    @Mock
    private BpmModuleConfig bpmConfig;

    @Mock
    private ProcessInstance processInstance;

    @Mock
    private KieSession kieSession;

    @Mock
    private KieClientConnector kieClientConnector;

    @InjectMocks
    private BpmManager bpmManager = new BpmManager();

    private boolean successNotification;
    private boolean errorNotification;

    @Test
    public void notificationTest() throws CoreException {
        successNotification = false;
        errorNotification = false;
        BpmTask task = new BpmTask("") {
            @Override
            public String getProcessId() {
                return "colors";
            }

            @Override
            protected Serializable getProcessParameters() throws CoreException {
                return new SimpleParameters();
            }
        };
        task.<BpmStringMapNotificationRest> addListener(RC_REPO_CREATION_SUCCESS, t -> {
            assertEquals("green", t.getData().get("color"));
            successNotification = true;
        });
        task.<BpmStringMapNotificationRest> addListener(RC_REPO_CREATION_ERROR, t -> {
            assertEquals("red", t.getData().get("color"));
            errorNotification = true;
        });
        bpmManager.startTask(task);

        // notify
        assertEquals(new Integer(1), task.getTaskId());
        BpmStringMapNotificationRest notification = mock(BpmStringMapNotificationRest.class);
        when(notification.getEventType()).thenReturn(RC_REPO_CREATION_SUCCESS.name());
        Map<String, String> data = new HashMap<>();
        data.put("color", "green");
        when(notification.getData()).thenReturn(data);
        bpmManager.notify(1, notification);

        assertTrue(successNotification);
        assertFalse(errorNotification);
    }

    class SimpleParameters implements Serializable {
        @Getter
        String color = "blue";
    }
}
