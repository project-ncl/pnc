/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.spi.exception.CoreException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static org.jboss.pnc.bpm.BpmEventType.valueOf;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

/**
 * Responsible for starting, keeping track of, and notifying BPM tasks.
 * Thread safe.
 *
 * @author Jakub Senko
 */
@ApplicationScoped
public class BpmManager {

    private static final Logger log = LoggerFactory.getLogger(BpmManager.class);

    private static final int AUTHENTICATION_TIMEOUT_S = 2 * 60;

    private Configuration configuration;
    private BpmModuleConfig bpmConfig;
    private int nextTaskId = 1;
    private Map<Integer, BpmTask> tasks = new HashMap<>();
    private KieSession session;

    @Deprecated
    public BpmManager() { //CDI workaround
    }

    @Inject
    public BpmManager(Configuration configuration) {
        this.configuration = configuration;
    }


    @PostConstruct
    private void init() throws CoreException {

        try {
            bpmConfig = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        } catch (ConfigurationParseException e) {
            throw new CoreException("BPM manager could not get its configuration.", e);
        }

        RuntimeEngine restSessionFactory;
        try {
            restSessionFactory = RemoteRuntimeEngineFactory.newRestBuilder()
                    .addDeploymentId(bpmConfig.getDeploymentId())
                    .addUrl(new URL(bpmConfig.getBpmInstanceUrl()))
                    .addUserName(bpmConfig.getUsername())
                    .addPassword(bpmConfig.getPassword())
                    .addTimeout(AUTHENTICATION_TIMEOUT_S)
                    .build();
        } catch (Exception e) {
            throw new CoreException("Could not initialize connection to BPM server at '" +
                    bpmConfig.getBpmInstanceUrl() + "' check that the URL is correct.", e);
        }

        session = restSessionFactory.getKieSession();
    }

    @PreDestroy
    private void dispose() {
        session.dispose();
    }


    private int getNextTaskId() {
        if (nextTaskId == MAX_VALUE) {
            nextTaskId = 1;
        }
        return nextTaskId++;
    }


    public synchronized boolean startTask(BpmTask task) throws CoreException {
        try {
            task.setTaskId(getNextTaskId());
            task.setBpmConfig(bpmConfig);
            ProcessInstance processInstance = session.startProcess(task.getProcessId(),
                    task.getExtendedProcessParameters());
            if (processInstance == null) {
                log.warn("Failed to create new process instance.");
                return false;
            }
            task.setProcessInstanceId(processInstance.getId());
            task.setProcessName(processInstance.getProcessId());
            log.debug("Created new process instance with id {}", task.getProcessInstanceId());
            tasks.put(task.getTaskId(), task);
            return true;

        } catch (Exception e) {
            throw new CoreException("Could not start BPM task '" + task + "'.", e);
        }
    }

    public synchronized void notify(int taskId, BpmNotificationRest notification) {
        log.debug("will process notification for taskId: {}", taskId);
        BpmTask task = tasks.get(taskId);
        if (task == null) {
            log.error("Cannot notify tasks with id: [{}]. Ids of tasks in progress: {}", taskId, tasks.keySet());
        } else {
            BpmEventType<?> bpmEventType = valueOf(notification.getEventType());
            if (bpmEventType != null && bpmEventType.getType().isInstance(notification)) {
                task.notify((BpmEventType<BpmNotificationRest>) bpmEventType, notification);
            }
        }

        log.info("finished notifying for taskId: {}", taskId);
    }

    /**
     * Regularly cleans finished BPM tasks asynchronously
     * Immediate cleanup is not usable because of NCL-2300
     */
    public void cleanup() {
        log.debug("Bpm manager tasks cleanup started");
        Map<Integer, BpmTask> clonedTasks = null;
        synchronized(this) {
            clonedTasks = new HashMap<>(this.tasks);
        }
        
        Set<Integer> toBeRemoved = clonedTasks.values().stream()
                .filter(t -> {
                    log.debug("attempting to fetch process instance from bpm");
                    ProcessInstance processInstance = session.getProcessInstance(t.getProcessInstanceId());
                    log.debug("fetched: {}", processInstance);
                    if (processInstance == null) // instance has been terminated from outside
                        return true;
                    int state = processInstance.getState();
                    return state == STATE_COMPLETED || state == STATE_ABORTED;
                })
                .map(BpmTask::getTaskId)
                .collect(Collectors.toSet());
        toBeRemoved.forEach(id -> tasks.remove(id));
        
        log.debug("Bpm manager tasks cleanup finished");
    }

    /**
     * This method solves backwards compatibility problem.
     * It will be removed soon.
     */
    @Deprecated
    public synchronized Integer getTaskIdByBuildId(int buildId) {
        List<Integer> result = tasks.values().stream()
                .filter(t -> t instanceof BpmBuildTask)
                .filter(t -> ((BpmBuildTask) t).getBuildTask().getId() == buildId)
                .map(BpmTask::getTaskId)
                .collect(Collectors.toList());
        if (result.size() > 1)
            throw new IllegalStateException("More that one task with the same build id: " + result);
        return result.size() == 1 ? result.get(0) : null;
    }

    public synchronized Collection<BpmTask> getActiveTasks() {
        return Collections.unmodifiableCollection(new HashSet<>(tasks.values()));
    }
}
