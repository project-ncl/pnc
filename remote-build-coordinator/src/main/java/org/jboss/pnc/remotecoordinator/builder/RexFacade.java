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

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.jboss.pnc.api.constants.HttpHeaders;
import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.model.MDCParameters;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.log.MDCUtils;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.mapper.api.BuildTaskMappers;
import org.jboss.pnc.model.User;
import org.jboss.pnc.remotecoordinator.BpmEndpointUrlFactory;
import org.jboss.pnc.remotecoordinator.rexclient.RexHttpClient;
import org.jboss.pnc.remotecoordinator.rexclient.exception.ConflictResponseException;
import org.jboss.pnc.remotecoordinator.rexclient.exception.TaskNotFoundException;
import org.jboss.pnc.rex.api.parameters.TaskFilterParameters;
import org.jboss.pnc.rex.common.enums.Mode;
import org.jboss.pnc.rex.dto.CreateTaskDTO;
import org.jboss.pnc.rex.dto.EdgeDTO;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.rex.dto.requests.CreateGraphRequest;
import org.jboss.pnc.spi.coordinator.BuildMeta;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.spi.exception.ScheduleConflictException;
import org.jboss.pnc.spi.exception.ScheduleErrorException;
import org.jboss.pnc.spi.exception.ScheduleException;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class RexFacade implements RexBuildScheduler, BuildTaskRepository {

    private static final Logger log = LoggerFactory.getLogger(RexFacade.class);

    public static final EnumSet<BuildCoordinationStatus> WAITING_STATES = EnumSet.of(
            BuildCoordinationStatus.NEW,
            BuildCoordinationStatus.ENQUEUED,
            BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);

    public static final EnumSet<BuildCoordinationStatus> RUNNING_STATES = EnumSet.of(BuildCoordinationStatus.BUILDING);

    public static final EnumSet<BuildCoordinationStatus> FINISHED_STATES = EnumSet.of(
            BuildCoordinationStatus.DONE,
            BuildCoordinationStatus.SYSTEM_ERROR,
            BuildCoordinationStatus.DONE_WITH_ERRORS,
            BuildCoordinationStatus.CANCELLED,
            BuildCoordinationStatus.REJECTED_FAILED_DEPENDENCIES,
            BuildCoordinationStatus.REJECTED_ALREADY_BUILT,
            BuildCoordinationStatus.BUILD_COMPLETED,
            BuildCoordinationStatus.REJECTED);
    public static final EnumSet<BuildCoordinationStatus> UNFINISHED_STATES;

    static {
        UNFINISHED_STATES = EnumSet.copyOf(WAITING_STATES);
        UNFINISHED_STATES.addAll(RUNNING_STATES);
    }

    private static final String INIT_DATA = "initData";

    private GlobalModuleGroup globalConfig;
    private BpmModuleConfig bpmConfig;
    private BuildTaskMappers mappers;
    private RexHttpClient rexClient;

    @Deprecated
    public RexFacade() { // CDI workaround
    }

    @Inject
    public RexFacade(
            GlobalModuleGroup globalConfig,
            BpmModuleConfig bpmConfig,
            BuildTaskMappers mappers,
            RexHttpClient rexClient) {
        this.globalConfig = globalConfig;
        this.bpmConfig = bpmConfig;
        this.mappers = mappers;
        this.rexClient = rexClient;
    }

    public void startBuilding(Graph<RemoteBuildTask> buildGraph, User user, Long buildConfigSetRecordId)
            throws ScheduleException {
        BpmEndpointUrlFactory bpmUrl = new BpmEndpointUrlFactory(bpmConfig.getBpmNewBaseUrl());

        Set<@NotNull @Valid EdgeDTO> edges = new HashSet<>();
        Map<@NotBlank String, @NotNull @Valid CreateTaskDTO> vertices = new HashMap<>();

        Collection<RemoteBuildTask> sourceVerticies = GraphUtils.unwrap(buildGraph.getVerticies());
        for (RemoteBuildTask buildTask : sourceVerticies) {
            vertices.put(buildTask.getId(), getCreateNewTaskDTO(bpmUrl, buildTask, user));
        }

        for (Edge<RemoteBuildTask> sourceEdge : buildGraph.getEdges()) {
            RemoteBuildTask from = sourceEdge.getFrom().getData();
            RemoteBuildTask to = sourceEdge.getTo().getData();
            EdgeDTO edge = new EdgeDTO(from.getId(), to.getId());
            edges.add(edge);
        }

        CreateGraphRequest createGraphRequest = new CreateGraphRequest(
                Objects.toString(buildConfigSetRecordId),
                edges,
                vertices);
        try {
            rexClient.start(createGraphRequest);
        } catch (ConflictResponseException e) {
            // re-try schedule
            log.warn("Build scheduling conflict.", e);
            throw new ScheduleConflictException("Build scheduling conflict.", e);
        } catch (Exception e) {
            log.error("Build scheduling error.", e);
            // no re-try
            throw new ScheduleErrorException("Build scheduling error.", e);
        }
    }

    @Override
    public void cancel(String taskId) throws RemoteRequestException {
        try {
            rexClient.cancel(taskId);
        } catch (Exception e) {
            throw new RemoteRequestException("Cannot cancel remote task.", e);
        }
    }

    @Override
    public Optional<BuildTaskRef> getSpecific(String taskId) throws RemoteRequestException, MissingDataException {
        try {
            TaskDTO taskDTO = rexClient.getSpecific(taskId);

            return Optional.of(mappers.toBuildTaskRef(taskDTO, getBuildMetadata(taskDTO)));
        } catch (TaskNotFoundException br) {
            return Optional.empty();
        } catch (MissingDataException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteRequestException("Cannot get task by id.", e);
        }
    }

    private static BuildMeta getBuildMetadata(TaskDTO taskDTO) throws MissingDataException {
        if (taskDTO.getCallerNotifications() == null
                || !(taskDTO.getCallerNotifications().getAttachment() instanceof BuildMeta)) {
            throw new MissingDataException("BuildMeta metadata missing for Build " + taskDTO.name);
        }
        return (BuildMeta) taskDTO.getCallerNotifications().getAttachment();
    }

    @Override
    public List<BuildTaskRef> getBuildTasksByBCSRId(Long buildConfigSetRecordId)
            throws RemoteRequestException, MissingDataException {
        try {
            ArrayList<BuildTaskRef> toReturn = new ArrayList<>();

            for (var task : rexClient.byCorrelation(Objects.toString(buildConfigSetRecordId, null))) {
                toReturn.add(mappers.toBuildTaskRef(task, getBuildMetadata(task)));
            }

            return toReturn;
        } catch (MissingDataException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteRequestException("Cannot get tasks by correlationId.", e);
        }
    }

    @Override
    @Deprecated // only used in the DefaultBuildCoordinator tests
    public Collection<BuildTaskRef> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<BuildTaskRef> getUnfinishedTasks() throws RemoteRequestException, MissingDataException {
        return getBuildTasksInState(UNFINISHED_STATES);
    }

    private Set<BuildTaskRef> getBuildTasksInState(EnumSet<BuildCoordinationStatus> states)
            throws RemoteRequestException, MissingDataException {
        try {
            Set<BuildTaskRef> set = new HashSet<>();
            TaskFilterParameters taskFilterParameters = toTaskFilterParameters(states);

            for (TaskDTO task : rexClient.getAll(taskFilterParameters)) {
                BuildTaskRef buildTaskRef = mappers.toBuildTaskRef(task, getBuildMetadata(task));
                set.add(buildTaskRef);
            }

            return set;
        } catch (MissingDataException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteRequestException("Cannot get tasks.", e);
        }
    }

    @Override
    @Deprecated
    public boolean isEmpty() {
        return false;
    }

    @Override
    @Deprecated
    public String getDebugInfo() {
        return null;
    }

    private TaskFilterParameters toTaskFilterParameters(EnumSet<BuildCoordinationStatus> states) {
        TaskFilterParameters taskFilterParameters = new TaskFilterParameters();

        Boolean finished = CollectionUtils.containsAny(FINISHED_STATES, states);
        taskFilterParameters.setFinished(finished);

        Boolean running = CollectionUtils.containsAny(RUNNING_STATES, states);
        taskFilterParameters.setRunning(running);

        Boolean waiting = CollectionUtils.containsAny(WAITING_STATES, states);
        taskFilterParameters.setWaiting(waiting);
        return taskFilterParameters;
    }

    private CreateTaskDTO getCreateNewTaskDTO(
            BpmEndpointUrlFactory bpmUrlFactory,
            RemoteBuildTask buildTask,
            User user) {
        String loginToken = user.getLoginToken();
        BpmBuildTask bpmBuildTask = new BpmBuildTask(toBuildTask(buildTask, user, new Date()), globalConfig);
        Map<String, Serializable> bpmTask = Collections
                .singletonMap("processParameters", bpmBuildTask.getProcessParameters());
        Map<String, Object> processParameters = new HashMap<>();
        processParameters.put("auth", Collections.singletonMap("token", loginToken));
        processParameters.put("mdc", new MDCParameters());
        processParameters.put("task", bpmTask);
        processParameters.put("submitTime", buildTask.getSubmitTime());

        Map<String, Map<String, Object>> bpmRequestBody = Collections.singletonMap(INIT_DATA, processParameters);

        List<Request.Header> headers = MDCUtils.getHeadersFromMDC()
                .entrySet().stream()
                .map(entry -> new Request.Header(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        headers.addAll(List.of(
                new Request.Header(HttpHeaders.CONTENT_TYPE_STRING, ContentType.APPLICATION_JSON.toString()),
                new Request.Header(HttpHeaders.ACCEPT_STRING, MediaType.APPLICATION_JSON),
                new Request.Header(HttpHeaders.AUTHORIZATION_STRING, "Bearer " + loginToken)));

        Request remoteStart = bpmUrlFactory.startProcessInstance(
                bpmConfig.getBpmNewDeploymentId(),
                bpmConfig.getBpmNewBuildProcessName(),
                buildTask.getId(),
                headers,
                bpmRequestBody);

        Request remoteCancel = bpmUrlFactory.processInstanceSignalByCorrelation(
                bpmConfig.getBpmNewDeploymentId(),
                buildTask.getId(),
                "CancelAll",
                headers);

        BuildMeta buildMetadata = mappers.toBuildMeta(buildTask);
        Request callback = Request.builder()
                .method(Request.Method.POST)
                .uri(URI.create(format("{0}/build-tasks/{1}/notify", globalConfig.getPncUrl(), buildTask.getId())))
                .headers(headers)
                .attachment(buildMetadata)
                .build();

        CreateTaskDTO createTaskDTO = new CreateTaskDTO(
                buildTask.getId(),
                BuildTaskMappers.toConstraint(buildTask.getBuildConfigurationAudited().getIdRev()),
                remoteStart,
                remoteCancel,
                callback,
                Mode.ACTIVE);
        return createTaskDTO;
    }

    @Deprecated // remove once fully migrated to external task scheduler
    private BuildTask toBuildTask(RemoteBuildTask buildTask, User user, Date startTime) {
        BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder().startTime(startTime).build();
        return BuildTask.build(
                buildTask.getBuildConfigurationAudited(),
                buildTask.getBuildOptions(),
                user,
                buildTask.getId(),
                buildSetTask,
                startTime,
                null,
                null,
                null);
    }
}
