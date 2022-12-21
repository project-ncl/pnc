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

import lombok.AllArgsConstructor;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.graph.GraphStructureException;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.logging.BuildTaskContext;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.ProcessStageUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.remotecoordinator.BuildCoordinationException;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.coordinator.Remote;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildSetStatusChangedEvent;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ScheduleConflictException;
import org.jboss.pnc.spi.repour.RepourResult;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Individual build submitted:
 * - collect all the tasks based on the dependencies
 *
 * Build set submitted:
 * - build all the submitted BC and do not create tasks for non submitted dependencies.
 *   Dependencies are used only to determine the order.
 *
 * No rebuild required:
 * - determine base on the DB status
 * - there must be no dependency scheduled for a rebuild
 * //TODO Jan: where do we order the builds (dependencies first) as we need that for NNR
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-20.
 */
@Remote
@ApplicationScoped
public class RemoteBuildCoordinator implements BuildCoordinator {

    private static final EnumSet<BuildStatus> REJECTED_STATES = EnumSet
            .of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

    private final Logger log = LoggerFactory.getLogger(RemoteBuildCoordinator.class);
    private static final Logger userLog = LoggerFactory
            .getLogger("org.jboss.pnc._userlog_.build-process-status-update");

    private SystemConfig systemConfig;
    private DatastoreAdapter datastoreAdapter;
    private Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;
    private Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    private RexBuildScheduler buildScheduler;

    private BuildTaskRepository taskRepository;

    private BuildTasksInitializer buildTasksInitializer;

    private GroupBuildMapper groupBuildMapper;
    private BuildMapper buildMapper;

    BuildConfigurationAuditedRepository bcaRepository;

    @Deprecated
    public RemoteBuildCoordinator() {
    } // workaround for CDI constructor parameter injection

    @Inject
    public RemoteBuildCoordinator(
            DatastoreAdapter datastoreAdapter,
            Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier,
            Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier,
            RexBuildScheduler buildScheduler,
            BuildTaskRepository taskRepository,
            BuildConfigurationAuditedRepository bcaRepository,
            SystemConfig systemConfig,
            GroupBuildMapper groupBuildMapper,
            BuildMapper buildMapper) {
        this.datastoreAdapter = datastoreAdapter;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildSetStatusChangedEventNotifier = buildSetStatusChangedEventNotifier;
        this.buildScheduler = buildScheduler;
        this.systemConfig = systemConfig;
        this.taskRepository = taskRepository;
        this.bcaRepository = bcaRepository;
        this.buildTasksInitializer = new BuildTasksInitializer(
                datastoreAdapter,
                systemConfig.getTemporaryBuildsLifeSpan());
        this.groupBuildMapper = groupBuildMapper;
        this.buildMapper = buildMapper;
    }

    /**
     * Run a single build. Uses the settings from the latest saved/audited build configuration.
     *
     * @param buildConfiguration The build configuration which will be used. The latest version of this build config
     *        will be built.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and
     *         version
     */
    @Override
    public BuildSetTask buildConfig(BuildConfiguration buildConfiguration, User user, BuildOptions buildOptions)
            throws BuildConflictException, CoreException {
        BuildConfigurationAudited buildConfigurationAudited = datastoreAdapter
                .getLatestBuildConfigurationAuditedInitializeBCDependencies(buildConfiguration.getId());
        return buildConfigurationAudited(buildConfigurationAudited, user, buildOptions);
    }

    private BuildSetTask notifyBuildStarted(ScheduleResult scheduleResult, User user, BuildConfigurationAudited buildConfigurationAudited)
            throws CoreException {
        // prepare the response and status update
        Collection<Vertex<RemoteBuildTask>> verticies = scheduleResult.buildGraph.getVerticies();
        Collection<RemoteBuildTask> buildTasks = GraphUtils.unwrap(verticies);
        String id = buildTasks
                .stream()
                .filter(t -> t.getBuildConfigurationAudited().getIdRev().equals(buildConfigurationAudited.getIdRev()))
                .findAny()
                .orElseThrow(() -> new CoreException(
                        "Missing task with IdRev: " + buildConfigurationAudited.getIdRev()))
                .getId();

        // return ID only
        BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder().build();
        BuildTask buildTask = BuildTask.build(
                buildConfigurationAudited,
                null,
                null,
                id,
                null,
                new Date(),
                null,
                null,
                null);

        buildSetTask.addBuildTask(buildTask);

        // nothing to save for a single build, just send the notifications
        buildTasks.stream()
                .forEach(task -> updateBuildTaskStatus(task, BuildCoordinationStatus.ENQUEUED, null, user));

        return buildSetTask;
    }

    private ScheduleResult build0(
            User user,
            BuildOptions buildOptions,
            BuildConfigurationAudited buildConfigurationAudited) throws BuildConflictException, CoreException {

        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        checkAllRunning(Collections.singleton(buildConfigurationAudited), unfinishedTasks);

        Graph<RemoteBuildTask> buildGraph;
        try {
            buildGraph = buildTasksInitializer.createBuildGraph(
                    buildConfigurationAudited,
                    user,
                    buildOptions,
                    unfinishedTasks);
        } catch (GraphStructureException e) {
            throw new CoreException("Cannot construct input graph: " + e.getMessage());
        }

        checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);

        Optional<BuildStatusWithDescription> validationFailedStatus = validateBuildConfigurationSetTask(
                buildGraph,
                buildOptions);

        BuildStatusWithDescription buildStatusWithDescription;
        BuildCoordinationStatus coordinationStatus;
        if (validationFailedStatus.isPresent()) {
            buildStatusWithDescription = validationFailedStatus.get();
            coordinationStatus = BuildCoordinationStatus.REJECTED;
        } else {
            try {
                buildScheduler.startBuilding(buildGraph, user);
            } catch (ScheduleConflictException e) {
                return new ScheduleResult(
                        buildGraph,
                        BuildCoordinationStatus.SYSTEM_ERROR,
                        new BuildStatusWithDescription(BuildStatus.SYSTEM_ERROR,"Failed to schedule remote tasks. " + e.getMessage()),
                        Set.of(),
                        e);
            }
            buildStatusWithDescription = new BuildStatusWithDescription(BuildStatus.BUILDING, null);
            coordinationStatus = BuildCoordinationStatus.ENQUEUED;
        }
        return new ScheduleResult(buildGraph, coordinationStatus, buildStatusWithDescription, Set.of());
    }

    /**
     * Run a single build. Uses the settings from the specific revision of a BuildConfiguration. The dependencies are
     * resolved by the BuildConfiguration relations and are used in the latest revisions
     *
     * @param buildConfigurationAudited A revision of a BuildConfiguration which will be used.
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build task
     * @throws BuildConflictException If there is already a build running with the same build configuration Id and
     *         revision
     */
    @Override
    public BuildSetTask buildConfigurationAudited(
            BuildConfigurationAudited buildConfigurationAudited,
            User user,
            BuildOptions buildOptions) throws BuildConflictException, CoreException {

        int attempt = 0;
        while (true) {
            try {
                ScheduleResult scheduleResult = build0(user, buildOptions, buildConfigurationAudited);
                if (scheduleResult.getException().isPresent()) {
                    attempt++;
                    if (attempt > systemConfig.getMaxScheduleRetries()) {
                        log.error("No more retries, failed to schedule remote tasks.", scheduleResult.getException());
                        throw new CoreException("No more retries, failed to schedule remote tasks.");
                    }
                    log.warn("Re-scheduling, attempt {}.", attempt);
                } else {
                    return notifyBuildStarted(scheduleResult, user, buildConfigurationAudited);
                }
            } catch (Throwable e) {
                String errorMessage = "Unexpected error while trying to schedule build.";
                log.error(errorMessage, e);
                throw new CoreException(errorMessage + " " + e.getMessage());
            }
        }
    }

    @Override
    @Deprecated
    public BuildSetTask buildSet(BuildConfigurationSet buildConfigurationSet, User user, BuildOptions buildOptions)
            throws CoreException {
        throw new UnsupportedOperationException("Should not be used.");
    }

    /**
     * Run a build of BuildConfigurationSet with BuildConfigurations in specific versions.
     *
     * The set of the BuildConfigurations to be executed is determined by the buildConfigurationSet entity. If there is
     * a revision of a BuildConfiguration available in the buildConfigurationAuditedsMap parameter, then this revision
     * is executed. If it's not available, latest revision of the BuildConfiguration will be executed.
     *
     * @param buildConfigurationSet The set of the configurations to be executed
     * @param buildConfigurationAuditedsMap A map BuildConfiguration::id:BuildConfigurationAudited of specific revisions
     *        of BuildConfigurations contained in the buildConfigurationSet
     * @param user The user who triggered the build.
     * @param buildOptions Customization of a build specified by user
     *
     * @return The new build set task
     * @throws CoreException Thrown if there is a problem initializing the build
     */
    @Override
    public BuildSetTask buildSet(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions) throws CoreException, BuildConflictException {

        int attempt = 0;
        while (true) {
            try {
                ScheduleResult scheduleResult = doBuildSet(buildConfigurationSet,
                        buildConfigurationAuditedsMap,
                        user,
                        buildOptions);
                if (scheduleResult.getException().isPresent()) {
                    attempt++;
                    if (attempt > systemConfig.getMaxScheduleRetries()) {
                        log.error("No more retries, failed to schedule remote tasks.", scheduleResult.getException());
                        return storeAndNotifyBuildSet(
                                buildConfigurationSet,
                                user,
                                buildOptions,
                                scheduleResult);
                    }
                    log.warn("Re-scheduling, attempt {}.", attempt);
                } else {
                    return storeAndNotifyBuildSet(
                            buildConfigurationSet,
                            user,
                            buildOptions,
                            scheduleResult);
                }
            } catch (Throwable e) {
                String errorMessage = "Unexpected error while trying to schedule build set.";
                log.error(errorMessage, e);
                BuildConfigSetRecord buildConfigSetRecord = storeBuildBuildConfingSetRecord(
                        buildConfigurationSet,
                        BuildStatus.SYSTEM_ERROR,
                        errorMessage + " " + e.getMessage(),
                        user,
                        buildOptions);
                // return BCSR.ID only
                return BuildSetTask.Builder.newBuilder().buildConfigSetRecord(buildConfigSetRecord).build();
            }
        }
    }

    /**
     * @return Optional.empty if there are still retry attempts
     */
    private ScheduleResult doBuildSet(
            BuildConfigurationSet buildConfigurationSet,
            Map<Integer, BuildConfigurationAudited> buildConfigurationAuditedsMap,
            User user,
            BuildOptions buildOptions) throws BuildConflictException, CoreException {
        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        checkAllRunning(buildConfigurationAuditedsMap.values(), unfinishedTasks);

        Graph<RemoteBuildTask> buildGraph;
        try {
            buildGraph = buildTasksInitializer.createBuildGraph(buildConfigurationSet,
                    buildConfigurationAuditedsMap,
                    user,
                    buildOptions,
                    unfinishedTasks);
        } catch (GraphStructureException e) {
            throw new CoreException("Cannot construct input graph: " + e.getMessage());
        }
        checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);

        Optional<BuildStatusWithDescription> validationFailedStatus = validateBuildConfigurationSetTask(buildGraph, buildOptions);

        BuildCoordinationStatus coordinationStatus;
        BuildStatusWithDescription buildStatusWithDescription;
        Collection<RemoteBuildTask> noRebuildTasks;

        if (validationFailedStatus.isPresent()) {
            buildStatusWithDescription = validationFailedStatus.get();
            coordinationStatus = BuildCoordinationStatus.REJECTED;
            noRebuildTasks = Collections.emptySet();
        } else {
            if (!buildOptions.isForceRebuild()) {
                noRebuildTasks = removeNRRTasks(buildGraph);
            } else {
                noRebuildTasks = Collections.emptySet();
            }

            try {
                buildScheduler.startBuilding(buildGraph, user);
            } catch (ScheduleConflictException e) {
                return new ScheduleResult(
                        buildGraph,
                        BuildCoordinationStatus.SYSTEM_ERROR,
                        new BuildStatusWithDescription(BuildStatus.SYSTEM_ERROR,"Failed to schedule remote tasks. " + e.getMessage()),
                        noRebuildTasks,
                        e);
            }
            buildStatusWithDescription = new BuildStatusWithDescription(BuildStatus.BUILDING, null);
            coordinationStatus = BuildCoordinationStatus.ENQUEUED;
        }

        return new ScheduleResult(buildGraph, coordinationStatus, buildStatusWithDescription, noRebuildTasks);
    }

    private class ScheduleResult {
        Graph<RemoteBuildTask> buildGraph;
        BuildCoordinationStatus coordinationStatus;
        BuildStatusWithDescription buildStatusWithDescription;
        Collection<RemoteBuildTask> noRebuildTasks;
        ScheduleConflictException exception;

        public ScheduleResult(
                Graph<RemoteBuildTask> buildGraph,
                BuildCoordinationStatus coordinationStatus,
                BuildStatusWithDescription buildStatusWithDescription,
                Collection<RemoteBuildTask> noRebuildTasks) {
            this.buildGraph = buildGraph;
            this.coordinationStatus = coordinationStatus;
            this.buildStatusWithDescription = buildStatusWithDescription;
            this.noRebuildTasks = noRebuildTasks;
        }

        public ScheduleResult(
                Graph<RemoteBuildTask> buildGraph,
                BuildCoordinationStatus coordinationStatus,
                BuildStatusWithDescription buildStatusWithDescription,
                Collection<RemoteBuildTask> noRebuildTasks,
                ScheduleConflictException e) {
            this.buildGraph = buildGraph;
            this.coordinationStatus = coordinationStatus;
            this.buildStatusWithDescription = buildStatusWithDescription;
            this.noRebuildTasks = noRebuildTasks;
            exception = e;
        }

        public Optional<ScheduleConflictException> getException() {
            return Optional.ofNullable(exception);
        }
    }

    private BuildSetTask storeAndNotifyBuildSet(
            BuildConfigurationSet buildConfigurationSet,
            User user,
            BuildOptions buildOptions,
            ScheduleResult scheduleResult) throws CoreException {
        BuildConfigSetRecord buildConfigSetRecord = storeBuildBuildConfingSetRecord(buildConfigurationSet,
                scheduleResult.buildStatusWithDescription.buildStatus,
                scheduleResult.buildStatusWithDescription.description, user, buildOptions);

        // save NRR records
        scheduleResult.noRebuildTasks.forEach(task -> completeNoBuild(task,
                scheduleResult.buildGraph, CompletionStatus.NO_REBUILD_REQUIRED, buildConfigSetRecord,
                user));

        GraphUtils.unwrap(scheduleResult.buildGraph.getVerticies()).stream()
                .forEach(task -> updateBuildTaskStatus(task, scheduleResult.coordinationStatus, buildConfigSetRecord, user));
        // return BCSR.ID only
        return BuildSetTask.Builder.newBuilder().buildConfigSetRecord(buildConfigSetRecord).build();
    }

    @Override
    public Optional<BuildTask> getSubmittedBuildTask(String buildId) {
        //TODO used only in completion callback, where there required data should already come from the Rex
        return Optional.empty();
    }

    /**
     * Returns a BuildStatus if there are validation errors (no build is required or not possible), otherwise Optional.empty is returned.
     */
    private Optional<BuildStatusWithDescription> validateBuildConfigurationSetTask(
            Graph buildGraph,
            BuildOptions buildOptions) throws CoreException {

        // Check if the given build set task is empty and update the status message appropriately
        if (buildGraph.isEmpty()) {
            return Optional.of(new BuildStatusWithDescription(BuildStatus.REJECTED, "Build config set is empty"));
        }

        // check if no rebuild is required
        if (!buildOptions.isForceRebuild()) {
            boolean noRebuildsRequired = checkIfNoRebuildIsRequired(buildGraph,
                    buildOptions.isImplicitDependenciesCheck(),
                    buildOptions.isTemporaryBuild(),
                    buildOptions.getAlignmentPreference());
            if (noRebuildsRequired) {
                return Optional.of(new BuildStatusWithDescription(BuildStatus.NO_REBUILD_REQUIRED,
                        "All build configs were previously built"));
            }
        }

        // check if there are cycles
        if (GraphUtils.hasCycle(buildGraph)) {
            return Optional.of(new BuildStatusWithDescription(BuildStatus.REJECTED, "Build config set has a cycle"));
        }
        return Optional.empty();
    }

    @AllArgsConstructor
    private class BuildStatusWithDescription {

        BuildStatus buildStatus;
        String description;
    }

    /**
     * Returns true if no build configurations needs a rebuild
     */
    private boolean checkIfNoRebuildIsRequired(Graph buildGraph, boolean checkImplicitDependencies, boolean temporaryBuild, AlignmentPreference alignmentPreference) {
        Collection<RemoteBuildTask> buildTasks = GraphUtils.unwrap(buildGraph.getVerticies());
        long requiresRebuild = buildTasks.stream()
                .filter(bt -> !bt.isAlreadyRunning()) //TODO what about already running ?
                .filter(bt -> bt.getNoRebuildCause().isEmpty())
                .count();
        log.debug("{} configurations require a rebuild.", requiresRebuild);
        return requiresRebuild == 0;
    }

    /**
     * Remove NO_REBUILD_REQUIRED them from the buildSetTask and return them.
     *
     * @return NO_REBUILD_REQUIRED tasks
     */
     private Collection<RemoteBuildTask> removeNRRTasks(Graph<RemoteBuildTask> buildGraph) {
        Set<Vertex<RemoteBuildTask>> toBuild = new HashSet<>();
        Set<Vertex<RemoteBuildTask>> notToBuild = new HashSet<>();

        for (Vertex<RemoteBuildTask> task : buildGraph.getVerticies()) {
            if (!toBuild.contains(task) && task.getData().getNoRebuildCause().isPresent()) {
                notToBuild.add(task);
            } else {
                markToBuild(task, toBuild, notToBuild);
            }
        }

        notToBuild.forEach(task -> {
            // NOTE: after removal NRR task can still be referenced as a dependency of other tasks
            buildGraph.removeVertex(task);
        });
        return GraphUtils.unwrap(notToBuild);
    }

    private void markToBuild(Vertex<RemoteBuildTask> task, Set<Vertex<RemoteBuildTask>> toBuild, Set<Vertex<RemoteBuildTask>> notToBuild) {
        toBuild.add(task);
        notToBuild.remove(task);
        markDependantsToBuild(task, toBuild, notToBuild);
    }

    private void markDependantsToBuild(Vertex<RemoteBuildTask> task, Set<Vertex<RemoteBuildTask>> toBuild, Set<Vertex<RemoteBuildTask>> notToBuild) {
        List<Vertex<RemoteBuildTask>> dependants = GraphUtils.getFromVerticies(task.getIncomingEdges());
        for (Vertex<RemoteBuildTask> dependant : dependants) {
            if (!toBuild.contains(dependant)) {
                markToBuild(dependant, toBuild, notToBuild);
            }
        }
    }

    @Override
    public boolean cancel (String buildTaskId) throws CoreException {
        // Logging MDC must be set before calling
        Optional<BuildTaskRef> taskOptional = taskRepository.getUnfinishedTasks().stream()
                .filter(buildTask -> buildTask.getId().equals(buildTaskId))
                .findAny();
        if (taskOptional.isPresent()) {
            log.debug("Cancelling task {}.", taskOptional.get());
            try {
                buildScheduler.cancel(taskOptional.get().getId());
                return true;
            } catch (CoreException e) {
                log.error("Failed to cancel task {}.", buildTaskId);
                return false;
            }
        } else {
            log.warn("Cannot find task {} to cancel.", buildTaskId);
            return false;
        }
    }

    @Override public Optional<BuildTaskContext> getMDCMeta (String buildTaskId){
        return getSubmittedBuildTasks().stream()
                .filter(buildTask -> buildTaskId.equals(buildTask.getId()))
                .map(this::getMDCMeta)
                .findAny();
    }

    private BuildTaskContext getMDCMeta (BuildTask buildTask){ //TODO
        boolean temporaryBuild = buildTask.getBuildOptions().isTemporaryBuild();
        return new BuildTaskContext(buildTask.getContentId(),
                buildTask.getUser().getId().toString(),
                temporaryBuild,
                ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), temporaryBuild));
    }

    @Override
    public boolean cancelSet(int buildConfigSetRecordId) {
        BuildConfigSetRecord record = datastoreAdapter.getBuildCongigSetRecordById(buildConfigSetRecordId);
        if (record == null) {
            log.error("Could not find buildConfigSetRecord with id : {}", buildConfigSetRecordId);
            return false;
        }
        log.debug("Cancelling Build Configuration Set: {}", buildConfigSetRecordId);
        Collection<BuildTask> buildTasks = getSubmittedBuildTasksBySetId(buildConfigSetRecordId);
        buildTasks.forEach(buildTask -> {
            try {
                MDCUtils.addBuildContext(getMDCMeta(buildTask));
                log.debug("Received cancel request for buildTaskId: {}.", buildTask.getId());
                cancel(buildTask.getId());
            } catch (CoreException e) {
                log.error("Unable to cancel the build [" + buildTask.getId() + "].", e);
            } finally {
                MDCUtils.removeBuildContext();
            }
        });

        // modifying of the record to Cancelled state is done in SetRecordUpdateJob
        return true;
    }

    public void updateBuildTaskStatus (BuildTask task, BuildCoordinationStatus status){
        throw new UnsupportedOperationException("This method should not be used.");
    }

    private void updateBuildTaskStatus (
            RemoteBuildTask task,
            BuildCoordinationStatus status,
            BuildConfigSetRecord buildConfigSetRecord,
            User user) {

//      BuildProgress

        Build build = buildMapper.fromRemoteBuildTask(task, buildConfigSetRecord, status, user);
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(build,
                null,
                BuildStatus.fromBuildCoordinationStatus(status));
        log.debug("Updated build task {} status to {}; new coord status: {}",
                task.getId(),
                buildStatusChanged,
                status);

        userLog.info("Build status updated to {}.", status);

        //TODO make sure it satisfies NCL-5885
        buildStatusChangedEventNotifier.fire(buildStatusChanged);
        log.debug("Fired buildStatusChangedEventNotifier after task {} status update to {}.",
                task.getId(),
                status);
    }

    /**
     * update status,
     * store BuildConfigSetRecord,
     * sendSetStatusChangeEvent
     *
     * @return
     */
    private BuildConfigSetRecord storeBuildBuildConfingSetRecord (BuildConfigurationSet buildConfigurationSet, BuildStatus
    status, String description, User user, BuildOptions buildOptions)
        throws CoreException {

        BuildConfigSetRecord buildConfigSetRecord = BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .startTime(new Date())
                .status(org.jboss.pnc.enums.BuildStatus.BUILDING)
                .temporaryBuild(buildOptions.isTemporaryBuild())
                .alignmentPreference(buildOptions.getAlignmentPreference())
                .build();

        updateBuildConfigSetRecordStatus(buildConfigSetRecord, status, description);
        return buildConfigSetRecord;
    }

    public void updateBuildConfigSetRecordStatus (BuildConfigSetRecord setRecord, BuildStatus status, String
    description)
        throws CoreException {
        log.info("Setting new status {} on buildConfigSetRecord.id {}. Description: {}.",
                status,
                setRecord.getId(),
                description);
        BuildStatus oldStatus = setRecord.getStatus();

        if (status.isFinal()) {
            setRecord.setEndTime(new Date());
        }
        setRecord.setStatus(status);

        // TODO: don't send the event if we haven't updated the row
        try {
            datastoreAdapter.saveBuildConfigSetRecord(setRecord);
            sendSetStatusChangeEvent(status, oldStatus, setRecord, description);
        } catch (DatastoreException de) {
            log.error("Failed to update build config set record status: ", de);
            throw new CoreException(de);
        }
    }

    private void sendSetStatusChangeEvent (BuildStatus status, BuildStatus oldStatus, BuildConfigSetRecord
    record, String description){
        BuildSetStatusChangedEvent event = new DefaultBuildSetStatusChangedEvent(oldStatus,
                status,
                groupBuildMapper.toDTO(record),
                description);
        log.debug("Notifying build set status update {}.", event);
        buildSetStatusChangedEventNotifier.fire(event);
    }

    public void completeNoBuild (
            RemoteBuildTask buildTask,
            Graph<RemoteBuildTask> buildGraph,
            CompletionStatus completionStatus,
            BuildConfigSetRecord buildConfigSetRecord,
            User user){
        String buildTaskId = buildTask.getId();

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (CompletionStatus.NO_REBUILD_REQUIRED.equals(completionStatus)) {
                updateBuildTaskStatus(buildTask, BuildCoordinationStatus.REJECTED_ALREADY_BUILT, buildConfigSetRecord, user);
                // TODO cancel should be here enable in 2.0 as CANCELed is not failed build
                // } else if (CompletionStatus.CANCELLED.equals(completionStatus)) {
                // updateBuildTaskStatus(buildTask, BuildCoordinationStatus.CANCELLED);
            } else {
                throw new BuildCoordinationException(String.format("Invalid status %s.", completionStatus));
            }

            log.debug("Storing no build required result. Id: {}", buildTaskId);

            Optional<Vertex<RemoteBuildTask>> taskVertex = GraphUtils.getVertex(buildGraph, buildTaskId);
            List<Vertex<RemoteBuildTask>> dependants = GraphUtils.getFromVerticies(taskVertex.get().getIncomingEdges());
            List<Base32LongID> dependantIds = GraphUtils.unwrap(dependants)
                    .stream()
                    .map(RemoteBuildTask::getId)
                    .map(Base32LongID::new)
                    .collect(Collectors.toList());
            List<Vertex<RemoteBuildTask>> dependencies = GraphUtils.getToVerticies(taskVertex.get().getOutgoingEdges());
            List<Base32LongID> dependencyIds = GraphUtils.unwrap(dependencies)
                    .stream()
                    .map(RemoteBuildTask::getId)
                    .map(Base32LongID::new)
                    .collect(Collectors.toList());

            BuildRecord buildRecord = datastoreAdapter.storeRecordForNoRebuild(
                    buildTask,
                    user,
                    dependencyIds,
                    dependantIds,
                    buildConfigSetRecord.getId());
            if (buildRecord.getStatus().completedSuccessfully()) {
                coordinationStatus = BuildCoordinationStatus.DONE;
            } else {
                log.warn(
                        "[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.",
                        buildTaskId,
                        buildRecord.getStatus());
                coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
            }

        } catch (Throwable e) {
            log.error("[buildTaskId: " + buildTaskId + "] Cannot store results to datastore.", e);
            coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        } finally {
            updateBuildTaskStatus(buildTask, coordinationStatus, buildConfigSetRecord, user);
        }
    }

    public void completeBuild(BuildTask buildTask, BuildResult buildResult){
        String buildTaskId = buildTask.getId();

        BuildCoordinationStatus coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
        try {
            if (buildResult.hasFailed()) {
                CompletionStatus operationCompletionStatus = buildResult.getCompletionStatus();

                switch (operationCompletionStatus) {
                    case SYSTEM_ERROR:
                        ProcessException exception;
                        if (buildResult.getProcessException().isPresent()) {
                            exception = buildResult.getProcessException().get();
                            log.debug("[buildTaskId: {}] Storing build result with exception {}.",
                                    buildTaskId,
                                    exception.getMessage());
                        } else if (buildResult.getRepourResult().isPresent()) {
                            RepourResult repourResult = buildResult.getRepourResult().get();
                            if (repourResult.getCompletionStatus().isFailed()) {
                                exception = new ProcessException("Repour completed with system error.");
                                log.debug(
                                        "[buildTaskId: {}] Storing build result with system error from repour: {}.",
                                        buildTaskId,
                                        repourResult.getLog());
                            } else {
                                exception = new ProcessException(
                                        "Build completed with system error but no exception.");
                                log.error(
                                        "[buildTaskId: {}] Storing build result with system_error and missing exception.",
                                        buildTaskId);
                            }
                        } else {
                            exception = new ProcessException(
                                    "Build completed with system error but no exception and no Repour result.");
                            log.error(
                                    "[buildTaskId: {}] Storing build result with system_error no exception and no Repour result.",
                                    buildTaskId);
                        }
//TODO                        datastoreAdapter.storeResult(buildTask, Optional.of(buildResult), exception);

                        coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                        break;

                    case CANCELLED:
                    case TIMED_OUT:
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
                                buildTaskId,
                                operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.CANCELLED;
                        break;

                    case FAILED:
                        log.debug("[buildTaskId: {}] Storing failed build result. FailedReasonStatus: {}",
                                buildTaskId,
                                operationCompletionStatus);
                        datastoreAdapter.storeResult(buildTask, buildResult);
                        coordinationStatus = BuildCoordinationStatus.DONE_WITH_ERRORS;
                        break;

                    case SUCCESS:
                        throw new BuildCoordinationException("Failed task with SUCCESS completion status ?!.");
                }

            } else {
                log.debug("[buildTaskId: {}] Storing success build result.", buildTaskId);
                BuildRecord buildRecord = datastoreAdapter.storeResult(buildTask, buildResult);
                if (buildRecord.getStatus().completedSuccessfully()) {
                    coordinationStatus = BuildCoordinationStatus.DONE;
                } else {
                    log.warn(
                            "[buildTaskId: {}] Something went wrong while storing the success result. The status has changed to {}.",
                            buildTaskId,
                            buildRecord.getStatus());
                    coordinationStatus = BuildCoordinationStatus.SYSTEM_ERROR;
                }
            }

            updateBuildTaskStatus(buildTask, coordinationStatus);

        } catch (Throwable e) {
            log.error("[buildTaskId: " + buildTaskId + "] Cannot store results to datastore.", e);
            updateBuildTaskStatus(buildTask, BuildCoordinationStatus.SYSTEM_ERROR);
        } finally {
            // Starts when the build execution completes
            ProcessStageUtils.logProcessStageEnd("FINALIZING_BUILD", "Finalizing completed.");
        }
    }

    public List<BuildTask> getSubmittedBuildTasks () {
        Collection<BuildTaskRef> unfinishedTasks = taskRepository.getUnfinishedTasks();
        return unfinishedTasks.stream()
                .map(this::toBuildTask)
                .collect(Collectors.toList());
    }

    private BuildTask toBuildTask(BuildTaskRef buildTask) {
        String contentId = ContentIdentityManager.getBuildContentId(buildTask.getId());
        GroupBuildRef buildGroup = null; //TODO
        BuildStatus status;
        IdRev idRev = buildTask.getIdRev();
        BuildConfigurationAudited buildConfigurationAudited = bcaRepository.queryById(idRev);

        return BuildTask.build(
                buildConfigurationAudited,
                null,
                user,
                buildTask.getId(),
                null,
                submitTime,
                null,
                contentId,
                null
        );
    }

    @Override
    public List<BuildTask> getSubmittedBuildTasksBySetId ( int buildConfigSetRecordId){
        return taskRepository.getBuildTasksByBCSRId(buildConfigSetRecordId);
    }

    @PostConstruct
    public void start () {
        log.info("The application is starting ...");
    }

    @PreDestroy
    public void destroy () {
        log.info("The application is shutting down ...");
    }

    /**
     * @throws BuildConflictException if all BCAs are in the unfinishedTasks.
     */
    private void checkAllRunning(Collection < BuildConfigurationAudited > BCAs, Collection < BuildTaskRef > unfinishedTasks)
            throws BuildConflictException {

        Set<IdRev> unfinished = unfinishedTasks.stream().map(t -> t.getIdRev()).collect(Collectors.toUnmodifiableSet());

        Set<IdRev> running = BCAs.stream().map(bca -> bca.getIdRev()).filter(unfinished::contains).collect(Collectors.toSet());

        if (running.size() == BCAs.size()) {
            String runningMessage = running.stream().map(IdRev::toString).collect(Collectors.joining(", "));
            throw new BuildConflictException("Active build task found using the same configuration BC(s): " + runningMessage);
        }
    }

    /**
     * @throws BuildConflictException
     */
    private void checkIfAnyDependencyOfAlreadyRunningIsSubmitted(Graph<RemoteBuildTask> buildGraph) throws BuildConflictException {
        for (Vertex<RemoteBuildTask> parent : buildGraph.getVerticies()) {
            if (parent.getData().isAlreadyRunning()) {
                Collection<RemoteBuildTask> children = parent.getOutgoingEdges();
                for (RemoteBuildTask child : children) {
                    if (!child.isAlreadyRunning()) {
                        throw new BuildConflictException(
                                "Submitted build " + parent.getData().getBuildConfigurationAudited().getName()
                                        + " is a dependency of already running build: "
                                        + child.getBuildConfigurationAudited().getName());
                    }
                }
            }
        }
    }
}
