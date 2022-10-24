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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.common.concurrent.MDCWrappers;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

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

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * This is an internal identifier, not one provided by BPM server.
     */
    private Integer taskId;

    private Long processInstanceId;

    @Getter
    private final List<BpmEvent> events = new ArrayList<>();

    private String processName;

    protected GlobalModuleGroup globalConfig;

    protected BpmModuleConfig config;

    private ConcurrentMap<BpmEventType, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * Users OAuth token used to authenticate requests on remote services
     */
    private final String accessToken;
    private Optional<Connector> connector = Optional.empty();
    private boolean jsonEncodedProcessParameters = true;

    public BpmTask(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getProcessName() {
        return processName;
    }

    /* package */ void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * Get the name of the BPM process for this task. Warning: The process MUST be asynchronous so it does not block BPM
     * manager.
     */
    public abstract String getProcessId();

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

    public void setBpmConfig(BpmModuleConfig config) {
        this.config = config;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    /* package */ void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * Listen to notifications from BPM process for this task.
     *
     * @param eventType event to follow
     */
    public <T extends BpmEvent> void addListener(BpmEventType eventType, Consumer<T> listener) {
        List<Consumer<?>> consumers = listeners.computeIfAbsent(eventType, (k) -> new ArrayList<>());
        consumers.add(MDCWrappers.wrap(listener));
    }

    public <T extends BpmEvent> void notify(BpmEventType eventType, T data) {
        List<Consumer<?>> listeners = this.listeners.computeIfAbsent(eventType, (k) -> new ArrayList<>());
        log.debug(
                "will notify bpm listeners for eventType: {}, matching listeners: {}, all listeners: {}",
                eventType,
                listeners,
                this.listeners);
        listeners.forEach(
                listener -> {
                    // Cast is OK because there is no unchecked method declaration to put wrong types
                    ((Consumer<T>) listener).accept(data);
                });
        events.add(data);
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
        if (isJsonEncodedProcessParameters()) {
            try {
                actualParameters.put("processParameters", MAPPER.writeValueAsString(processParameters));
            } catch (JsonProcessingException e) {
                throw new CoreException(
                        "Could not serialize process processParameters '" + processParameters + "'.",
                        e);
            }
        } else {
            actualParameters.put("processParameters", processParameters);
        }

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

        // Setting OTEL data from MDC
        MDCUtils.getCustomContext("trace_id").ifPresent(v -> {
            log.debug("Setting otel parameter traceId: {}", v);
            actualParameters.put("traceId", v);
        });
        MDCUtils.getCustomContext("span_id").ifPresent(v -> {
            log.debug("Setting otel parameter spanId: {}", v);
            actualParameters.put("spanId", v);
        });
        return actualParameters;
    }

    public void setJsonEncodedProcessParameters(boolean jsonEncodedProcessParameters) {
        this.jsonEncodedProcessParameters = jsonEncodedProcessParameters;
    }

    protected boolean isJsonEncodedProcessParameters() {
        return jsonEncodedProcessParameters;
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
        return "BpmTask{" + "taskId=" + taskId + ", processInstanceId=" + processInstanceId + ", events=" + events
                + ", processName='" + processName + '\'' + ", config=" + config + ", listeners=" + listeners
                + ", accessToken='***'" + '}';
    }

    public Optional<Connector> getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = Optional.ofNullable(connector);
    }
}
