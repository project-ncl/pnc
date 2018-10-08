/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * <h3>Build task queue.</h3>
 *
 * The queue consists of 5 collections:
 * <ul>
 * <li>taskSets - set of currently processed task sets</li>
 * <li>tasksInProgress - set of tasks that are being executed at the moment</li>
 * <li>readyTasks - queue of tasks that are ready to be executed but are waiting for a free executor (and throttling mechanism)</li>
 * <li>waitingTasksWithCallbacks - tasks waiting for a dependency. As soon as their dependencies are built, they are moved to readyTasks.
 * The waiting tasks are mapped to callbacks that are executed upon the transfer
 * </li>
 * <li>unfinishedTasks - tasks either waiting, ready or in progress.
 * This collection is introduced to fix the race condition in {@link #take()},
 * where a task is taken from readyTask, and later put into tasksInProgress and the method cannot be synchronized</li>
 * </ul>
 *
 * TODO: 1. taskSets can probably be removed <br>
 * TODO: 2. Currently it throttles the number of tasks in progress. Is this necessary?
 * <p/>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 4/18/16
 * Time: 12:47 PM
 */
@ApplicationScoped
public class BuildQueue {

    private final Logger log = LoggerFactory.getLogger(BuildQueue.class);

    private Configuration configuration;

    private final Set<BuildTask> unfinishedTasks = new HashSet<>();

    private final BlockingQueue<BuildTask> readyTasks = new LinkedBlockingQueue<>();
    private final Map<BuildTask, Runnable> waitingTasksWithCallbacks = new HashMap<>();
    private final Set<BuildTask> tasksInProgress = ConcurrentHashMap.newKeySet();
    private final Set<BuildSetTask> taskSets = new HashSet<>();

    private final Semaphore availableBuildSlots = new Semaphore(0);



    @Inject
    public BuildQueue(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unused")
    @Deprecated
    public BuildQueue() {
    }

    /**
     * Add a new, ready to build task to queue
     *
     * @param task task to be enqueued
     */
    public synchronized boolean addReadyTask(BuildTask task) {
        if (!task.readyToBuild()) {
            throw new IllegalArgumentException("a not ready task added to the queue: " + task);
        }
        unfinishedTasks.add(task);
        log.debug("adding task: {}", task);
        readyTasks.add(task);
        return true;
    }

    /**
     * Add a task that is waiting for dependencies
     * @param task task that is not ready to build
     * @param taskReadyCallback a callback to be invoked when the task becomes ready
     */
    public synchronized void addWaitingTask(BuildTask task, Runnable taskReadyCallback) {
        unfinishedTasks.add(task);
        log.debug("adding waiting task: {}", task);
        waitingTasksWithCallbacks.put(task, taskReadyCallback);
    }

    /**
     * Enqueue all tasks of a task set
     *
     * @param taskSet task set to be built
     */
    public synchronized void enqueueTaskSet(BuildSetTask taskSet) {
        log.debug("adding task set: {}", taskSet);
        taskSets.add(taskSet);
    }

    /**
     * remove task set from queue. This method should be invoked after whole task set is processed
     *
     * @param taskSet processed task set
     */
    public synchronized void removeSet(BuildSetTask taskSet) {
        log.debug("removing task set: {}", taskSet);
        taskSets.remove(taskSet);
    }

    /**
     * remove task from the queue.
     * This method should be invoked if the task is completed, either successfully, or with error (including rejected build)
     *
     * @param task task to be removed
     */
    public synchronized void removeTask(BuildTask task) {
        log.debug("removing task: {}", task);
        if (tasksInProgress.remove(task)) {
            availableBuildSlots.release();
        }
        if (readyTasks.remove(task)) {
            log.debug("The task {} has been removed from readyTasks.", task);
        }

        if (waitingTasksWithCallbacks.remove(task) != null) {
            log.debug("The task {} has been removed from waitingTasks.", task);
        }

        if (unfinishedTasks.remove(task)) {
            log.debug("The task {} has been removed from unfinishedTasks.", task);
        }
    }

    /**
     * Trigger searching for ready tasks in the waiting queue.
     * This method should be invoked if one task has finished and there's a possibility that other tasks became ready to be built.
     */
    public synchronized void executeNewReadyTasks() {
        List<BuildTask> newReadyTasks = extractReadyTasks();
        log.debug("starting new ready tasks. New ready tasks: {}", newReadyTasks);
        readyTasks.addAll(newReadyTasks);
    }

    /**
     * Get build task for given build configuration from the queue.
     *
     * @param buildConfigAudited build configuration
     * @return Optional.of(build task for the configuration) if build task is enqueued/in progress, Optional.empty() otherwise
     */
    public synchronized Optional<BuildTask> getTask(BuildConfigurationAudited buildConfigAudited) {
        Optional<BuildTask> ready = readyTasks.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        Optional<BuildTask> waiting = waitingTasksWithCallbacks.keySet().stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        Optional<BuildTask> inProgress = tasksInProgress.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        return ready.isPresent() ? ready : waiting.isPresent() ? waiting : inProgress;
    }

    /**
     * List all waiting, ready and in progress tasks
     *
     * @return list of all build tasks in the queue
     */
    public synchronized List<BuildTask> getSubmittedBuildTasks() {

        return new ArrayList<>(unfinishedTasks);

    }

    public BuildTask take() throws InterruptedException {
        availableBuildSlots.acquire();
        log.info("Consumer is ready to go, waiting for task");
        //FIXME not thread safe: when a task is taken from readyTasks it is not in the tasksInProgress for a short time
        // race condition hit while working on SkippingBuiltConfigsTest.shouldNotTriggerTheSameBuildConfigurationViaDependency
        // to avoid race condition getUnfinishedTask is used instead of getTask
        BuildTask task = readyTasks.take();
        log.info("Got task: {}, will start processing", task);
        tasksInProgress.add(task);
        return task;
    }


    public synchronized boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return unfinishedTasks.contains(buildTask);
    }

    public synchronized Optional<BuildTask> getUnfinishedTask(BuildConfigurationAudited buildConfigurationAudited) {
        return unfinishedTasks.stream().filter(buildTask -> buildTask.getBuildConfigurationAudited().equals(buildConfigurationAudited)).findFirst();
    }

    public synchronized Set<BuildTask> getUnfinishedTasks() {
        return new HashSet<>(unfinishedTasks);
    }

    private List<BuildTask> extractReadyTasks() {
        List<BuildTask> noLongerWaitingTasks = waitingTasksWithCallbacks.keySet().stream()
                .filter(BuildTask::readyToBuild)
                .collect(Collectors.toList());

        noLongerWaitingTasks.forEach(task -> {
            waitingTasksWithCallbacks.get(task).run();
            waitingTasksWithCallbacks.remove(task);
        });

        return noLongerWaitingTasks;
    }

    @PostConstruct
    public void initSemaphore()  {
        int maxConcurrentBuilds = 10;
        try {
            SystemConfig systemConfig = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));

            maxConcurrentBuilds = systemConfig.getCoordinatorMaxConcurrentBuilds();
        } catch (ConfigurationParseException e) {
            log.error("Error parsing configuration, using 10 max concurrent builds in BuildQueue", e);
        }
        availableBuildSlots.release(maxConcurrentBuilds);
    }

    @Override
    public synchronized String toString() {
        return "BuildQueue{" +
                "readyTasks=" + readyTasks +
                ", waitingTasks=" + waitingTasksWithCallbacks +
                ", tasksInProgress=" + tasksInProgress +
                ", taskSets=" + taskSets +
                '}';
    }

    public synchronized boolean isEmpty() {
        return tasksInProgress.isEmpty() && waitingTasksWithCallbacks.isEmpty() && readyTasks.isEmpty()
                && unfinishedTasks.isEmpty() && taskSets.isEmpty();
    }

    public synchronized String getDebugInfo() {
        String info = "=====================\nQUEUE STATE:\n=====================\n" +
                "Available build slots: " + availableBuildSlots.availablePermits() + "\n" +
                "Queue length:" + availableBuildSlots.getQueueLength() + "\n" +
                "\n=====================\nTASKS IN PROGRESS:\n=====================\n" +
                tasksInProgress +
                "\n=====================\nREADY TASKS:\n=====================\n" +
                readyTasks +
                "\n=====================\nWAITING TASKS:\n=====================\n" +
                waitingTasksWithCallbacks.keySet() +
                "\n=====================\nALL UNFINISHED TASKS:\n=====================\n" +
                unfinishedTasks +
                "\n=====================\nTASK SETS:\n=====================\n" +
                taskSets;

        return info;
    }
}
