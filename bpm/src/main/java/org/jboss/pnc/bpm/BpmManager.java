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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.util.StringUtils;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    private AtomicInteger nextTaskId = new AtomicInteger(1);
    private Map<Integer, BpmTask> tasks = new ConcurrentHashMap<>();
    private KieSession session;

    private static final String SIGNAL_CANCEL = "CANCELLED";

    private Set<Consumer<BpmTask>> newTaskAddedSubscribes = new HashSet<>();

    @Deprecated
    public BpmManager() { //CDI workaround
    }

    @Inject
    public BpmManager(BpmModuleConfig bpmConfig) {
        this.bpmConfig = bpmConfig;
    }


    @PostConstruct
    public void init() throws CoreException {
        session = initKieSession();
    }

    protected KieSession initKieSession() throws CoreException {
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

        return restSessionFactory.getKieSession();
    }

    @PreDestroy
    private void dispose() {
        session.dispose();
    }


    private int getNextTaskId() {
        return nextTaskId.getAndIncrement();
    }


    public boolean startTask(BpmTask task) throws CoreException {
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
            tasks.put(task.getTaskId(), task);

            log.debug("Notifying new task added {}.", task.getTaskId());
            notifyNewTaskAdded(task);

            log.debug("Created new process linked to task: {}", task);
            return true;

        } catch (Exception e) {
            throw new CoreException("Could not start BPM task '" + task + "'.", e);
        }
    }

    private void notifyNewTaskAdded(BpmTask task) {
        log.debug("Notifying {} subscribers for new task {}.", newTaskAddedSubscribes.size(), task.getTaskId());
        newTaskAddedSubscribes.forEach(subscriber -> {
            log.debug("Notifying subscriber {}.", subscriber.getClass());
            subscriber.accept(task);
        });
    }

    public boolean subscribeToNewTasks(Consumer<BpmTask> consumer) {
        log.debug("Subscribing new tasks consumer {}.", consumer);
        return newTaskAddedSubscribes.add(consumer);
    }

    public boolean unSubscribeFromNewTasks(Consumer<BpmTask> consumer) {
        return newTaskAddedSubscribes.remove(consumer);
    }

    public boolean cancelTask(BpmTask bpmTask) {
        String cancelEndpointUrl = StringUtils.stripEndingSlash(bpmConfig.getBpmInstanceUrl()) + "/nclcancelhandler";

        URI uri;
        try {
            URIBuilder uriBuilder = new URIBuilder(cancelEndpointUrl);
            uriBuilder.addParameter("processInstanceId", bpmTask.getProcessInstanceId().toString());
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("Unable to cancel process id: " + bpmTask.getProcessId(), e);
            return false;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            log.debug("Triggering the cancellation using url: {}", uri.toString());
            HttpGet httpget = new HttpGet(uri);
            httpget.setConfig(RequestConfig.custom()
                    .setConnectionRequestTimeout(bpmConfig.getCancelConnectionRequestTimeout())
                    .setConnectTimeout(bpmConfig.getCancelConnectTimeout())
                    .setSocketTimeout(bpmConfig.getCancelSocketTimeout())
                    .build());
            CloseableHttpResponse httpResponse = httpClient.execute(httpget);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            httpResponse.close();
            log.info("Cancel request for bpmTask.id: {} and processInstanceId: {} completed with status: {}.",
                    bpmTask.getTaskId(), bpmTask.getProcessInstanceId(), statusCode);
            return statusCode == 200;
        } catch (IOException e) {
            log.error("Unable to cancel process id: " + bpmTask.getProcessId(), e);
            return false;
        }
    }

    public void notify(int taskId, BpmNotificationRest notification) { //TODO do not use RestModel down here.
        log.debug("Will process notification for taskId: {}; BpmNotificationRest: {}.", taskId, notification.toString());
        Optional<BpmTask> maybeTask = getTaskById(taskId);
        if (!maybeTask.isPresent()) {
            log.error("Cannot notify tasks with id: [{}]. Ids of tasks in progress: {}", taskId, tasks.keySet());
        }
        maybeTask.ifPresent(task -> {
            BpmEventType bpmEventType = nullableValueOf(notification.getEventType());
            if (bpmEventType != null && bpmEventType.getType().isInstance(notification)) {
                log.debug("Notifying task: {}, eventType: {}, notification: {}.", task.getTaskId(), bpmEventType, notification.toString());
                task.notify(bpmEventType, notification);
            }
        });

        log.info("finished notifying for taskId: {}", taskId);
    }

    /**
     * Regularly cleans finished BPM tasks asynchronously
     * Immediate cleanup is not usable because of NCL-2300
     */
    public void cleanup() { //TODO remove tasks immediately after the completion, see: BuildTaskEndpoint.buildTaskCompleted
        log.debug("Bpm manager tasks cleanup started");

        if (session == null) {
            log.error("Kie session not available.");
        }

        Map<Integer, BpmTask> clonedTaskMap = new HashMap<>(this.tasks);

        Set<Integer> toBeRemoved = clonedTaskMap.values().stream()
                .filter(bpmTask -> {
                    if (bpmTask == null) {
                        log.warn("Listing invalid entry for removal from the tasks list.");
                        return true;
                    }
                    log.debug("Attempting to fetch process instance for bpmTask: {}.", bpmTask.getTaskId());
                    Long processInstanceId = bpmTask.getProcessInstanceId();
                    ProcessInstance processInstance = session.getProcessInstance(processInstanceId);
                    log.debug("fetched: {}", processInstance);
                    if (processInstance == null) // instance has been terminated from outside
                        return true;
                    int state = processInstance.getState();
                    return state == STATE_COMPLETED || state == STATE_ABORTED;
                })
                .map(BpmTask::getTaskId)
                .collect(Collectors.toSet());
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
     * This method solves backwards compatibility problem.
     * It will be removed soon.
     */
    @Deprecated
    public Integer getTaskIdByBuildId(int buildId) {
        List<Integer> result = tasks.values().stream()
                .filter(t -> t instanceof BpmBuildTask)
                .filter(t -> ((BpmBuildTask) t).getBuildTask().getId() == buildId)
                .map(BpmTask::getTaskId)
                .collect(Collectors.toList());
        if (result.size() > 1)
            throw new IllegalStateException("More that one task with the same build id: " + result);
        return result.size() == 1 ? result.get(0) : null;
    }

    public Collection<BpmTask> getActiveTasks() {
        return Collections.unmodifiableCollection(new HashSet<>(tasks.values()));
    }

    public Optional<BpmTask> getTaskById(int taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }
}
