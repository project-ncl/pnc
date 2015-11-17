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

package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.core.builder.executor.BuildExecutionTask;
import org.jboss.pnc.core.builder.executor.BuildExecutor;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutorMock implements BuildExecutor {

    private Logger log = LoggerFactory.getLogger(BuildExecutorMock.class);

    private Map<Integer, BuildExecutionTask> runningExecutions = new HashMap<>();

    private ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public BuildExecutionTask build(BuildConfiguration buildConfiguration, BuildConfigurationAudited buildConfigAudited, User user, Consumer<BuildStatus> onComplete, Set<Integer> buildRecordSetIds, Integer buildConfigSetRecordId, Integer buildTaskId, Date submitTime) throws CoreException {
        BuildExecutionTask buildExecutionTask = BuildExecutionTask.build(
                buildTaskId,
                buildConfiguration,
                buildConfigAudited,
                user,
                buildRecordSetIds,
                buildConfigSetRecordId,
                Optional.empty(),
                buildTaskId,
                submitTime
        );

        //TODO recollect to running instances in case of system failure
        startBuilding(buildExecutionTask, onComplete);

        return buildExecutionTask;
    }

    @Override
    public void startBuilding(BuildExecutionTask buildExecutionTask, Consumer<BuildStatus> onComplete) throws CoreException {
        runningExecutions.put(buildExecutionTask.getId(), buildExecutionTask);
        Consumer<BuildStatus> onCompleteInternal = (buildStatus) -> {
            log.debug("Removing buildExecutionTask [" + buildExecutionTask.getId() + "] form list of running tasks.");
            runningExecutions.remove(buildExecutionTask.getId());
            onComplete.accept(buildStatus);
        };

        CompletableFuture.supplyAsync(() -> mockBuild(), executor)
                .thenApplyAsync((buildTook) -> complete(onCompleteInternal), executor);
    }

    private Integer complete(Consumer<BuildStatus> onCompleteInternal) {
        onCompleteInternal.accept(BuildStatus.DONE);
        return -1;
    }

    private Integer mockBuild() {

        int sleep = RandomUtils.randInt(50, 500);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            log.warn("Mock build interrupted.", e);
        }
        return sleep;
    }

//    private CompletableFuture<Integer> mockBuild() {
//        CompletableFuture<Integer> waitToCompleteFuture = new CompletableFuture<>();
//        int sleep = RandomUtils.randInt(50, 500);
//        try {
//            Thread.sleep(sleep);
//        } catch (InterruptedException e) {
//            log.warn("Mock build interrupted.", e);
//        }
//        waitToCompleteFuture.complete(sleep);
//
//        return waitToCompleteFuture;
//    }
//
    @Override
    public BuildExecutionTask getRunningExecution(int buildExecutionTaskId) {
        return runningExecutions.get(buildExecutionTaskId);
    }

    @Override
    public void shutdown() {

    }
}
