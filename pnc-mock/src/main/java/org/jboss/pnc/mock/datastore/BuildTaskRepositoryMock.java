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
package org.jboss.pnc.mock.datastore;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildTaskRepositoryMock implements BuildTaskRepository {
    private final Map<String, BuildTask> tasks = new ConcurrentHashMap<>();

    @Override
    public BuildTask getTask(String id) {
        return tasks.get(id);
    }

    @Override
    public Optional<BuildTask> getTask(
            BuildConfigurationAudited buildConfigAudited,
            Set<BuildCoordinationStatus> states) {
        List<BuildTask> tasks = this.tasks.values()
                .stream()
                .filter(
                        t -> states.contains(t.getStatus())
                                && t.getBuildConfigurationAudited().getId().equals(buildConfigAudited.getId())
                                && t.getBuildConfigurationAudited().getRev().equals(buildConfigAudited.getRev()))
                .collect(Collectors.toList());
        switch (tasks.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(tasks.get(0));
            default:
                throw new IllegalStateException(
                        "Multiple tasks in states " + states + " found for build config with id "
                                + buildConfigAudited.getId());
        }
    }

    @Override
    public List<BuildTask> getBuildTasksInState(Set<BuildCoordinationStatus> states) {
        return this.tasks.values().stream().filter(t -> states.contains(t.getStatus())).collect(Collectors.toList());
    }

    @Override
    public List<BuildTask> getBuildTasksByBCSRId(Integer buildConfigSetRecordId) {
        return tasks.values()
                .stream()
                .filter(t -> buildConfigSetRecordId.equals(t.getBuildConfigSetRecordId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BuildTask> getAll() {
        return tasks.values();
    }

    @Override
    public Collection<BuildTask> getUnfinishedTasks() {
        return tasks.values().stream().filter(task -> !task.getStatus().isCompleted()).collect(Collectors.toList());
    }

    @Override
    public boolean isEmpty() {
        return tasks.isEmpty() || tasks.values().stream().allMatch(task -> task.getStatus().isCompleted());
    }

    @Override
    public String getDebugInfo() {
        return "null";
    }

    public void addTask(BuildTask task) {
        this.tasks.put(task.getId(), task);
    }

    public void removeTask(BuildTask task) {
        this.tasks.remove(task.getId());
    }
}
