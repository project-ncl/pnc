/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.impl;

import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.dto.internal.bpm.BPMTask;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.BpmController;
import org.jboss.pnc.mapper.api.BPMTaskMapper;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class BpmControllerImpl implements BpmController {

    @Inject
    private BpmManager bpmManager;

    @Inject
    private BPMTaskMapper bpmTaskMapper;

    public Page<BPMTask> getBPMTasks(int pageIndex, int pageSize) {

        Collection<BpmTask> tasks = bpmManager.getActiveTasks();

        int totalHits = tasks.size();
        int totalPages = (totalHits + pageSize - 1) / pageSize;

        List<BPMTask> pagedTasks = tasks.stream()
                .sorted()
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .map(bpmTaskMapper::toDTO)
                .collect(Collectors.toList());

        return new Page<>(pageIndex, pageSize, totalPages, totalHits, pagedTasks);
    }

    public BPMTask getBPMTaskById(int taskId) {

        Optional<BpmTask> task = bpmManager.getTaskById(taskId);
        return bpmTaskMapper.toDTO(task.orElse(null));
    }
}
