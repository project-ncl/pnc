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
package org.jboss.pnc.bpm;

import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.bpm.BpmEventType.nullableValueOf;

/**
 * Responsible for starting, keeping track of, and notifying BPM tasks. Thread safe.
 *
 * @author Jakub Senko
 */
@ApplicationScoped
public class BpmManager {

    private static final Logger log = LoggerFactory.getLogger(BpmManager.class);

    static final int AUTHENTICATION_TIMEOUT_S = 2 * 60;

    private GlobalModuleGroup globalConfig;
    private BpmModuleConfig bpmConfig;
    private AtomicInteger nextTaskId = new AtomicInteger(1);
    private Map<Integer, BpmTask> tasks = new ConcurrentHashMap<>();
    private KieClientConnector kieConnector;
    private RestConnector restConnector;

    private static final String SIGNAL_CANCEL = "CANCELLED";

    private Set<Consumer<BpmTask>> newTaskAddedSubscribes = new HashSet<>();

    @Deprecated
    public BpmManager() { // CDI workaround
    }

    @Inject
    public BpmManager(GlobalModuleGroup globalConfig, BpmModuleConfig bpmConfig) {
        this.globalConfig = globalConfig;
        this.bpmConfig = bpmConfig;
    }

    @PostConstruct
    public void init() throws CoreException {
        kieConnector = new KieClientConnector(globalConfig, bpmConfig);
        restConnector = new RestConnector(bpmConfig);
    }

    @PreDestroy
    private void dispose() {
        kieConnector.close();
        restConnector.close();
    }

    public int getNextTaskId() {
        return nextTaskId.getAndIncrement();
    }

    public boolean startTask(BpmTask task) throws CoreException {
        try {
            task.setTaskId(getNextTaskId());
            task.setGlobalConfig(globalConfig);
            task.setBpmConfig(bpmConfig);
            if (!task.getConnector().isPresent()) {
                defineConnector(task);
            }

            String processId = task.getProcessId();
            Long processInstanceId = task.getConnector()
                    .get()
                    .startProcess(processId, task.getExtendedProcessParameters(), task.getAccessToken());
            task.setProcessInstanceId(processInstanceId);
            task.setProcessName(processId);
            tasks.put(task.getTaskId(), task);

            log.debug("Notifying new task added {}.", task.getTaskId());
            notifyNewTaskAdded(task);

            log.debug("Created new process linked to task: {}", task);
            return true;
        } catch (Exception e) {
            throw new CoreException("Could not start BPM task '" + task + "'.", e);
        }
    }

    public void defineConnector(BpmTask task) {
        if (ConnectorSelector.useNewProcess(task, bpmConfig.isNewBpmForced())) {
            task.setConnector(restConnector);
            task.setJsonEncodedProcessParameters(false);
        } else {
            task.setConnector(kieConnector);
            task.setJsonEncodedProcessParameters(true);
        }
    }

    private void notifyNewTaskAdded(BpmTask task) {
        log.debug("Notifying {} subscribers for new task {}.", newTaskAddedSubscribes.size(), task.getTaskId());
        newTaskAddedSubscribes.forEach(subscriber -> {
            log.debug("Notifying subscriber {}.", subscriber.getClass());
            subscriber.accept(task);
        });
    }

    public boolean cancelTask(BpmTask bpmTask) {
        return bpmTask.getConnector().get().cancel(bpmTask.getProcessInstanceId(), bpmTask.getAccessToken());
    }

    public void notify(int taskId, BpmEvent notification) { // TODO do not use RestModel down here.
        log.debug(
                "Will process notification for taskId: {}; BpmNotificationRest: {}.",
                taskId,
                notification.toString());
        Optional<BpmTask> maybeTask = getTaskById(taskId);
        if (!maybeTask.isPresent()) {
            log.error("Cannot notify tasks with id: [{}]. Ids of tasks in progress: {}", taskId, tasks.keySet());
        }
        maybeTask.ifPresent(task -> {
            BpmEventType bpmEventType = nullableValueOf(notification.getEventType());
            if (bpmEventType != null && bpmEventType.getType().isInstance(notification)) {
                log.debug(
                        "Notifying task: {}, eventType: {}, notification: {}.",
                        task.getTaskId(),
                        bpmEventType,
                        notification.toString());
                task.notify(bpmEventType, notification);
            }
        });

        log.info("finished notifying for taskId: {}", taskId);
    }

    /**
     * Regularly cleans finished BPM tasks asynchronously Immediate cleanup is not usable because of NCL-2300
     */
    public void cleanup() { // TODO remove tasks immediately after the completion, see:
                            // BuildTaskEndpoint.buildTaskCompleted
        log.debug("Bpm manager tasks cleanup started");

        Map<Integer, BpmTask> clonedTaskMap = new HashMap<>(this.tasks);

        Set<Integer> toBeRemoved = clonedTaskMap.values().stream().filter(bpmTask -> {
            if (bpmTask == null) {
                log.warn("Listing invalid entry for removal from the tasks list.");
                return true;
            }
            log.debug("Attempting to fetch process instance for bpmTask: {}.", bpmTask.getTaskId());
            Long processInstanceId = bpmTask.getProcessInstanceId();
            return bpmTask.getConnector().get().isProcessInstanceCompleted(processInstanceId);
        }).map(BpmTask::getTaskId).collect(Collectors.toSet());
        toBeRemoved.forEach(id -> {
            BpmTask removed = tasks.remove(id);
            if (removed != null) {
                log.debug("Removed bpmTask.id: {}.", removed.getTaskId());
            } else {
                log.warn("Unable to remove bpmTask.id: {}.", id);
            }
        });

        log.debug("Bpm manager tasks cleanup finished");
    }

    /**
     * This method solves backwards compatibility problem. It will be removed soon.
     */
    @Deprecated
    public Integer getTaskIdByBuildId(long buildId) {
        List<Integer> result = tasks.values()
                .stream()
                .filter(t -> t instanceof BpmBuildTask)
                .filter(t -> ((BpmBuildTask) t).getBuildTask().getId() == buildId)
                .map(BpmTask::getTaskId)
                .collect(Collectors.toList());
        if (result.size() > 1) {
            throw new IllegalStateException("More that one task with the same build id: " + result);
        }
        return result.size() == 1 ? result.get(0) : null;
    }

    public Collection<BpmTask> getActiveTasks() {
        return Collections.unmodifiableCollection(new HashSet<>(tasks.values()));
    }

    public Optional<BpmTask> getTaskById(int taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public void remove(Integer taskId) {
        BpmTask removed = tasks.remove(taskId);
        if (removed != null) {
            log.debug("Removed task id: {}.", removed.getTaskId());
        } else {
            log.warn(
                    "Trying to remove non-existing task with id: [{}]. Ids of tasks in progress: {}",
                    taskId,
                    tasks.keySet());
        }
    }
}
