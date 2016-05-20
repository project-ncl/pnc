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
package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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
 * The queue consists of 4 collections:
 * <ul>
 * <li>tasksInProgress - set of tasks that are being executed at the moment</li>
 * <li>readyTasks - queue of tasks that are ready to be executed but are waiting for a free executor (and throttling mechanism)</li>
 * <li>waitingTasks - tasks waiting for a dependency. As soon as the dependency is built, they are moved to readyTasks</li>
 * </ul>
 *
 * NOTE: This implementation is not entirely thread safe. It might happen that {@link #isBuildAlreadySubmitted(BuildTask)} returns fails
 * while the build task is being taken from the queue. The same applies to the results of {@link BuildQueue#getSubmittedBuildTasks()}
 *
 *
 * TODO: Currently it throttles the number of tasks in progress. Is this necessary?
 * <p/>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 4/18/16
 * Time: 12:47 PM
 */
@ApplicationScoped
public class BuildQueue {

    private final Logger log = LoggerFactory.getLogger(BuildQueue.class);

    private Configuration configuration;

    private final BlockingQueue<BuildTask> readyTasks = new LinkedBlockingQueue<>();
    private final Set<BuildTask> waitingTasks = ConcurrentHashMap.newKeySet();
    private final Set<BuildTask> tasksInProgress = ConcurrentHashMap.newKeySet();

    private final Semaphore availableBuildSlots = new Semaphore(0);

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


    @Inject
    public BuildQueue(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unused")
    @Deprecated
    public BuildQueue() {
    }

    /**
     * Add a new task to queue
     *
     * @param task task to be enqueued
     */
    public void enqueueTask(BuildTask task) {
        log.debug("adding task: {}", task);
        addTask(task);
    }

    /**
     * remove task from the queue.
     * This method should be invoked if the task is completed, either successfully, or with error (including rejected build)
     *
     * @param task task to be removed
     */
    public void removeTask(BuildTask task) {
        log.debug("removing task: {}", task);
        if (tasksInProgress.remove(task)) {
            availableBuildSlots.release();
        }
        readyTasks.remove(task);
        waitingTasks.remove(task);
    }

    /**
     * Trigger searching for ready tasks in the waiting queue.
     * This method should be invoked if one task has finished and there's a possibility that other tasks became ready to be built.
     */
    public void executeNewReadyTasks() {
        List<BuildTask> newReadyTasks = extractReadyTasks();
        readyTasks.addAll(newReadyTasks);
        log.debug("starting new ready tasks. New ready tasks: {}", newReadyTasks);
    }

    /**
     * Get build task for given build configuration from the queue.
     *
     * @param buildConfigAudited build configuration
     * @return Optional.of(build task for the configuration) if build task is enqueued/in progress, Optional.empty() otherwise
     */
    public Optional<BuildTask> getTask(BuildConfigurationAudited buildConfigAudited) {
        Optional<BuildTask> ready = readyTasks.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        Optional<BuildTask> waiting = waitingTasks.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        Optional<BuildTask> inProgress = tasksInProgress.stream().filter(bt -> bt.getBuildConfigurationAudited().equals(buildConfigAudited)).findAny();
        return ready.isPresent() ? ready : waiting.isPresent() ? waiting : inProgress;
    }

    /**
     * List all waiting, ready and in progress tasks
     *
     * @return list of all build tasks in the queue
     */
    public List<BuildTask> getSubmittedBuildTasks() {
        ArrayList<BuildTask> result = new ArrayList<>();
        result.addAll(waitingTasks);
        result.addAll(readyTasks);
        result.addAll(tasksInProgress);
        return result;
    }

    public BuildTask take() throws InterruptedException {
        availableBuildSlots.acquire();
        log.info("Consumer is ready to go, waiting for task");
        BuildTask task = readyTasks.take();
        log.info("Got task: {}, will start processing", task);
        tasksInProgress.add(task);
        return task;
    }


    public boolean isBuildAlreadySubmitted(BuildTask buildTask) {
        return tasksInProgress.contains(buildTask) || readyTasks.contains(buildTask) || waitingTasks.contains(buildTask);
    }

    private List<BuildTask> extractReadyTasks() {
        List<BuildTask> newReadyTasks = waitingTasks.stream()
                .filter(BuildTask::readyToBuild)
                .collect(Collectors.toList());
        waitingTasks.removeAll(newReadyTasks);
        return newReadyTasks;
    }

    private void addTask(BuildTask task) {
        if (task.readyToBuild()) {
            readyTasks.add(task);
        } else {
            waitingTasks.add(task);
        }
    }

    @Override
    public String toString() {
        return "BuildQueue{" +
                "readyTasks=" + readyTasks +
                ", waitingTasks=" + waitingTasks +
                ", tasksInProgress=" + tasksInProgress + '}';
    }

    public boolean isEmpty() {
        return tasksInProgress.isEmpty() && waitingTasks.isEmpty() && readyTasks.isEmpty();
    }
}
