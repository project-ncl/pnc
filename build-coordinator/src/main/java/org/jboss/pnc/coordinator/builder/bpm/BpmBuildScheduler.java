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

package org.jboss.pnc.coordinator.builder.bpm;

import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.coordinator.builder.BuildScheduler;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private BpmManager manager;

    private BuildResultMapper mapper;

    public static final String schedulerId = "bpm-build-scheduler";

    @Override
    public String getId() {
        return schedulerId;
    }

    @Deprecated
    public BpmBuildScheduler() { // CDI workaround
    }

    @Inject
    public BpmBuildScheduler(BpmManager manager, BuildResultMapper mapper) {
        this.manager = manager;
        this.mapper = mapper;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildResult> onComplete) throws CoreException {
        try {
            BpmBuildTask task = new BpmBuildTask(buildTask);
            task.<BuildResultRest> addListener(BpmEventType.BUILD_COMPLETE, b -> onComplete.accept(mapper.toEntity(b)));
            manager.startTask(task);
        } catch (Exception e) {
            throw new CoreException("Error while trying to startBuilding with BpmBuildScheduler.", e);
        }
    }

    @Override
    public boolean cancel(BuildTask buildTask) {
        Optional<BpmBuildTask> taskOptional = manager.getActiveTasks()
                .stream()
                .filter(bpmTask -> bpmTask instanceof BpmBuildTask)
                .map(bpmTask -> (BpmBuildTask) bpmTask)
                .filter(bpmTask -> bpmTask.getBuildTask().getId().equals(buildTask.getId()))
                .findAny();
        if (taskOptional.isPresent()) {
            return manager.cancelTask(taskOptional.get());
        } else {
            return false;
        }
    }
}
