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
package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.coordinator.BuildTask;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BuildTaskRepository {

    BuildTask getTask(String id);

    Optional<BuildTask> getTask(BuildConfigurationAudited buildConfigAudited, Set<BuildCoordinationStatus> states);

    List<BuildTask> getBuildTasksInState(Set<BuildCoordinationStatus> states);

    List<BuildTask> getBuildTasksByBCSRId(Integer buildConfigSetRecordId);

    Collection<BuildTask> getAll();

    Collection<BuildTask> getUnfinishedTasks();

    boolean isEmpty();

    // TODO do we still need this?
    String getDebugInfo();
}
