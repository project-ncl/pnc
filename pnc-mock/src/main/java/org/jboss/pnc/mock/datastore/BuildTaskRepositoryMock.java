/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.datastore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.exception.RemoteRequestException;

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
    public List<BuildTaskRef> getBuildTasksByBCSRId(Base32LongID buildConfigSetRecordId) {
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

    public void clear() {
        tasks.clear();
    }
}
