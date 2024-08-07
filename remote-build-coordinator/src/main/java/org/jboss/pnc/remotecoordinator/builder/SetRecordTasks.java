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
package org.jboss.pnc.remotecoordinator.builder;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.Remote;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.CoreException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigSetRecordId;

@ApplicationScoped
@Slf4j
// TODO: make it run on a single instance if easily doable
public class SetRecordTasks {

    private BuildTaskRepository taskRepository;

    private BuildRecordRepository recordRepository;

    private Datastore datastore;

    private BuildCoordinator buildCoordinator;

    @Deprecated // CDI
    public SetRecordTasks() {
    }

    @Inject
    public SetRecordTasks(
            BuildTaskRepository taskRepository,
            BuildRecordRepository recordRepository,
            Datastore datastore,
            @Remote BuildCoordinator buildCoordinator) {
        this.taskRepository = taskRepository;
        this.recordRepository = recordRepository;
        this.datastore = datastore;
        this.buildCoordinator = buildCoordinator;
    }

    /**
     * @throws CoreException
     */
    @Transactional
    public void updateConfigSetRecordsStatuses() throws CoreException {
        log.debug("triggered the job");
        // #1 query for unfinished BCSR
        // for each -> BTasks -> check status
        //
        // see BTasks -> if BCSD NEW and running BTasks -> change to RUNNING
        // see BTasks -> if not BTasks -> check BRs -> decide final status
        // see BTasks ->
        Collection<BuildConfigSetRecord> setRecords = datastore.findBuildConfigSetRecordsInProgress();
        log.debug("BCSRs in progress: {}", setRecords);
        for (BuildConfigSetRecord setRecord : setRecords) {
            updateConfigSetRecordStatus(setRecord);
        }
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord) throws CoreException {
        log.debug("Checking BuildConfigSetRecord[{}] for status update", setRecord.getId());
        List<BuildTaskRef> buildTasks = taskRepository.getBuildTasksByBCSRId(setRecord.getId());
        List<BuildRecord> buildRecords = recordRepository.queryWithBuildConfigurationSetRecordId(setRecord.getId());

        BuildStatus effectiveState = getEffectiveState(setRecord, buildTasks, buildRecords);
        if (setRecord.getStatus() != effectiveState) {
            updateConfigSetRecordStatus(setRecord, effectiveState);
            log.debug("BuildConfigSetRecord[{}] changes status to {}", setRecord, effectiveState);
        } else {
            log.debug("BuildConfigSetRecord[{}] didn't change its status", setRecord);
        }
    }

    private BuildStatus getEffectiveState(
            BuildConfigSetRecord setRecord,
            Collection<BuildTaskRef> buildTasks,
            Collection<BuildRecord> buildRecords) {
        if (buildTasks.isEmpty() && buildRecords.isEmpty()) {
            log.error(
                    "BuildConfigSetRecord[{}] has no tasks and no build records, setting status to {}",
                    setRecord,
                    BuildStatus.REJECTED);
            return BuildStatus.REJECTED;
        }
        Set<String> ids = buildRecords.stream()
                .map(BuildRecord::getId)
                .map(Base32LongID::getId)
                .collect(Collectors.toSet());

        List<BuildTaskRef> effectiveBuildTasks = buildTasks.stream()
                .filter(task -> !ids.contains(task.getId()))
                .collect(Collectors.toList());

        Set<BuildStatus> buildStatuses = Stream.concat(
                buildRecords.stream().map(BuildRecord::getStatus),
                effectiveBuildTasks.stream().map(BuildTaskRef::getStatus).map(BuildStatus::fromBuildCoordinationStatus))
                .collect(Collectors.toSet());

        return determineStatus(buildStatuses);
    }

    private BuildStatus determineStatus(Set<BuildStatus> statuses) {
        if (statuses.stream().anyMatch(status -> !status.isFinal())) {
            return BuildStatus.BUILDING;
        }

        if (statuses.contains(BuildStatus.CANCELLED)) {
            return BuildStatus.CANCELLED;
        }

        if (statuses.size() == 1 && statuses.contains(BuildStatus.NO_REBUILD_REQUIRED)) {
            return BuildStatus.NO_REBUILD_REQUIRED;
        }

        if (statuses.stream().allMatch(BuildStatus::completedSuccessfully)) {
            return BuildStatus.SUCCESS;
        }

        return BuildStatus.FAILED;
    }

    private void updateConfigSetRecordStatus(BuildConfigSetRecord setRecord, BuildStatus effectiveState)
            throws CoreException {
        buildCoordinator.storeAndNotifyBuildConfigSetRecord(setRecord, effectiveState, "");
    }
}
