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

import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.exception.RemoteRequestException;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildTaskRepositoryMock implements BuildTaskRepository {
    private final Map<String, BuildTaskRef> tasks = new ConcurrentHashMap<>();

    public BuildTaskRef getTask(String id) {
        return tasks.get(id);
    }

    @Override
    public Optional<BuildTaskRef> getSpecific(String taskId) throws RemoteRequestException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<BuildTaskRef> getBuildTasksByBCSRId(Long buildConfigSetRecordId) {
        return tasks.values()
                .stream()
                .filter(t -> buildConfigSetRecordId.equals(t.getBuildConfigSetRecordId()))
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public Collection<? extends BuildTaskRef> getAll() {
        return tasks.values();
    }

    @Override
    public Collection<BuildTaskRef> getUnfinishedTasks() {
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

    public void addTask(BuildTaskRef task) {
        this.tasks.put(task.getId(), task);
    }

    public void removeTask(BuildTaskRef task) {
        this.tasks.remove(task.getId());
    }
}
