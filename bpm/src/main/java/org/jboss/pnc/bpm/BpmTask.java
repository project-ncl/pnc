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
package org.jboss.pnc.bpm;

import lombok.EqualsAndHashCode;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Parent type of all BPM tasks. Task is a representation of BPM process execution, keeps track of the remote process
 * instance and handles notifications. Class implements Comparable based on the taskId, so they can be sorted at REST
 * endpoint. BPM tasks do not have to be thread safe.
 *
 * @author Jakub Senko
 */
@EqualsAndHashCode(of = "taskId")
public abstract class BpmTask implements Comparable<BpmTask> {

    private static final Logger log = LoggerFactory.getLogger(BpmTask.class);

    /**
     * This is an internal identifier, not one provided by BPM server.
     */
    private Integer taskId;

    protected GlobalModuleGroup globalConfig;

    /**
     * Users OAuth token used to authenticate requests on remote services
     */
    private final String accessToken;

    public BpmTask(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Provide parameters to the BPM process. The JSON representation of this map will be available in the BPM process
     * as "processParameters" variable. Some parameters, such as "taskId" are automatically added, not into the
     * "processParameters" variable, but 'one level higher' and directly accessible from process variables.
     *
     * @return a map of process parameters
     * @throws CoreException
     */
    protected abstract Serializable getProcessParameters() throws CoreException;

    public void setGlobalConfig(GlobalModuleGroup globalConfig) {
        this.globalConfig = globalConfig;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Extend process parameters from the task with additional useful information, such as pncBaseUrl and taskId, needed
     * for notifications. Before use, taskId MUST be assigned.
     *
     * @throws CoreException
     */
    public Map<String, Object> getExtendedProcessParameters() throws CoreException {
        Serializable processParameters = getProcessParameters();
        requireNonNull(processParameters);
        Map<String, Object> actualParameters = new HashMap<>();
        actualParameters.put("processParameters", processParameters);

        // global not process related parameters
        actualParameters.put("taskId", taskId);
        actualParameters.put("usersAuthToken", accessToken);
        MDCUtils.getUserId().ifPresent(v -> {
            log.debug("Setting process parameter userId: {}", v);
            actualParameters.put("userId", v);
        });
        MDCUtils.getRequestContext().ifPresent(v -> {
            log.debug("Setting process parameter logRequestContext: {}", v);
            actualParameters.put("logRequestContext", v);
        });
        MDCUtils.getProcessContext().ifPresent(v -> {
            log.debug("Setting process parameter logProcessContext: {}", v);
            actualParameters.put("logProcessContext", v);
        });
        return actualParameters;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public int compareTo(BpmTask other) {
        requireNonNull(other);
        if (taskId == null) {
            return other.getTaskId() == null ? 0 : -1;
        }
        if (other.getTaskId() == null) {
            return 1;
        }
        return taskId.compareTo(other.getTaskId());
    }

    @Override
    public String toString() {
        return "BpmTask{" + "taskId=" + taskId + ", accessToken='***'" + '}';
    }
}
