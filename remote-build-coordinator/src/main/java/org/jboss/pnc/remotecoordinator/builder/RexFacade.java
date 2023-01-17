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
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.bpm.model.ComponentBuildParameters;
import org.jboss.pnc.bpm.model.MDCParameters;
import org.jboss.pnc.bpm.task.BpmBuildTask;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.utils.ContentIdentityManager;
import org.jboss.pnc.remotecoordinator.BpmEndpointUrlFactory;
import org.jboss.pnc.rex.api.parameters.TaskFilterParameters;
import org.jboss.pnc.rex.common.enums.Mode;
import org.jboss.pnc.rex.common.enums.State;
import org.jboss.pnc.rex.dto.CreateTaskDTO;
import org.jboss.pnc.rex.dto.EdgeDTO;
import org.jboss.pnc.rex.dto.TaskDTO;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.DefaultBuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class RexFacade implements RexBuildScheduler, BuildTaskRepository {

    public static final EnumSet<BuildCoordinationStatus> WAITING_STATES = EnumSet.of(
            BuildCoordinationStatus.NEW,
            BuildCoordinationStatus.ENQUEUED,
            BuildCoordinationStatus.WAITING_FOR_DEPENDENCIES);

    public static final EnumSet<BuildCoordinationStatus> RUNNING_STATES = EnumSet.of(
            BuildCoordinationStatus.BUILDING);

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

    Object rexClient = null;

    @Inject
    LoggedInUser loggedInUser;

    @Deprecated
    public RexFacade() { // CDI workaround
    }

    @Inject
    public RexFacade(GlobalModuleGroup globalConfig, BpmModuleConfig bpmConfig, LoggedInUser loggedInUser) {
        this.globalConfig = globalConfig;
        this.bpmConfig = bpmConfig;
        this.loggedInUser = loggedInUser;
    }

    public void startBuilding(Graph<RemoteBuildTask> buildGraph, User user) throws CoreException {
        BpmEndpointUrlFactory bpmUrl = new BpmEndpointUrlFactory(bpmConfig.getBpmNewBaseUrl());

        Set<@NotNull @Valid EdgeDTO> edges = new HashSet<>();
        Map<@NotBlank String, @NotNull @Valid CreateTaskDTO> vertices = new HashMap<>();

        Collection<RemoteBuildTask> sourceVerticies = GraphUtils.unwrap(buildGraph.getVerticies());
        for (RemoteBuildTask buildTask : sourceVerticies) {
            vertices.put(buildTask.getId(), getCreateNewTaskDTO(bpmUrl, buildTask, user));
        }

        for (Edge<RemoteBuildTask> sourceEdge: buildGraph.getEdges()) {
            RemoteBuildTask from = sourceEdge.getFrom().getData();
            RemoteBuildTask to = sourceEdge.getTo().getData();
            EdgeDTO edge = new EdgeDTO(from.getId(), to.getId());
            edges.add(edge);
        }

//        CreateGraphRequest createGraphRequest = new CreateGraphRequest(edges, vertices);
//        return rexClient.start(createGraphRequest, loggedInUser.getTokenString());
    }

    @Override
    public boolean cancel(String taskId) throws CoreException {
//        return rexClient.cancel(taskId, loggedInUser.getTokenString());
        return false; //TODO
    }

    @Override
    public List<BuildTaskRef> getBuildTasksByBCSRId(Integer buildConfigSetRecordId) {
        // used by getBuildIdsInTheGroup
        return null; //TODO
    }

    private Set<TaskDTO> getBuildTasksInState(EnumSet<BuildCoordinationStatus> states) {
        TaskFilterParameters taskFilterParameters = toTaskFilterParameters(states);
//        return rexClient.getAll(taskFilterParameters);
        return Collections.emptySet(); //TODO
    }

    @Override
    @Deprecated //only used in the tests
    public Collection<BuildTaskRef> getAll() {
        return null;
    }

    @Override
    public Collection<BuildTaskRef> getUnfinishedTasks() {
        Set<BuildTaskRef> runningBuildTasks = new HashSet<>();
        Set<TaskDTO> unfinishedBuildTasks = getBuildTasksInState(UNFINISHED_STATES);
        unfinishedBuildTasks.forEach(t -> {
            IdRev idRev = ConstraintMapper.toIdRev(t.getConstraint());
            String buildId = t.getName();
            String buildContentId = ContentIdentityManager.getBuildContentId(buildId);
//            BuildCoordinationStatus status = toBuildCordinationStatus(t.getState());
            Map<String, Object> attachment = (Map<String, Object>) t.getRemoteStart().getAttachment();
            Map<String, Object> initData = (Map<String, Object>) attachment.get(this.INIT_DATA);
            ComponentBuildParameters parameters = (ComponentBuildParameters) initData.get("processParameters");
            Instant submitTime = (Instant) initData.get("submitTime");
            String buildConfigSetRecordID = t.getCorrelationID();

            BuildTaskRef buildTask = new DefaultBuildTaskRef(
                    buildId,
                    idRev,
                    buildConfigSetRecordID,
                    buildContentId,
                    parameters.getBuildExecutionConfiguration().getUser().getUsername(),
                    submitTime,
                    toBuildCoordinationStatus(t.getState()));
            runningBuildTasks.add(buildTask);
        });
        return runningBuildTasks;
    }

    private BuildCoordinationStatus toBuildCoordinationStatus(State state) {
        switch (state) {
            case NEW:
                return BuildCoordinationStatus.NEW;
            case ENQUEUED:
                return BuildCoordinationStatus.ENQUEUED;
            case UP:
                return BuildCoordinationStatus.BUILDING;
            case SUCCESSFUL:
                return BuildCoordinationStatus.BUILD_COMPLETED;
            case FAILED:
                return BuildCoordinationStatus.DONE_WITH_ERRORS;
        }
        throw new UnsupportedOperationException("Needs to be implemented."); //TODO implement all statues
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

    private String getLoginToken(BuildSetTask buildSetTask) {
        return buildSetTask.getBuildTasks().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There are no builds in the set."))
                .getUser().getLoginToken();
    }

    private TaskFilterParameters toTaskFilterParameters(EnumSet<BuildCoordinationStatus> states) {
        TaskFilterParameters taskFilterParameters = new TaskFilterParameters();

        Boolean finished =  CollectionUtils.containsAny(FINISHED_STATES, states);
        taskFilterParameters.setFinished(finished);

        Boolean running = CollectionUtils.containsAny(RUNNING_STATES, states);
        taskFilterParameters.setRunning(running);

        Boolean waiting = CollectionUtils.containsAny(WAITING_STATES, states);
        taskFilterParameters.setWaiting(waiting);
        return taskFilterParameters;
    }

    private CreateTaskDTO getCreateNewTaskDTO(BpmEndpointUrlFactory bpmUrlFactory, RemoteBuildTask buildTask, User user) throws CoreException {
        String loginToken = loggedInUser.getTokenString();
        BpmBuildTask bpmBuildTask = new BpmBuildTask(toBuildTask(buildTask, user, new Date()), globalConfig);
        Map<String, Serializable> bpmTask = Collections.singletonMap("processParameters", bpmBuildTask.getProcessParameters());
        Map<String, Object> processParameters = new HashMap<>();
        processParameters.put("auth", Collections.singletonMap("token", loginToken));
        processParameters.put("mdc", new MDCParameters());
        processParameters.put("task", bpmTask);
        processParameters.put("submitTime", buildTask.getSubmitTime());

        Map<String, Map<String, Object>> bpmRequestBody = Collections.singletonMap(INIT_DATA, processParameters);

        List<Request.Header> headers = List.of(
                new Request.Header(HttpHeaders.CONTENT_TYPE_STRING, ContentType.APPLICATION_JSON.toString()),
                new Request.Header(HttpHeaders.ACCEPT_STRING, MediaType.APPLICATION_JSON),
                new Request.Header(HttpHeaders.AUTHORIZATION_STRING, "Bearer " + loginToken)
        );
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

        Request callback = new Request(
                Request.Method.POST,
                URI.create(globalConfig.getPncUrl() + ""), //TODO URL
                headers);

        CreateTaskDTO createTaskDTO = new CreateTaskDTO(
                buildTask.getId(),
                ConstraintMapper.toConstraint(buildTask.getBuildConfigurationAudited().getIdRev()),
                remoteStart,
                remoteCancel,
                callback,
                Mode.ACTIVE
        );
        return createTaskDTO;
    }

    @Deprecated //remove once fully migrated to external task scheduler
    private BuildTask toBuildTask(RemoteBuildTask buildTask, User user, Date startTime) {
        BuildSetTask buildSetTask = BuildSetTask.Builder.newBuilder()
                .startTime(startTime)
                .build();
        return BuildTask.build(
                buildTask.getBuildConfigurationAudited(),
                buildTask.getBuildOptions(),
                user,
                buildTask.getId(),
                buildSetTask,
                startTime,
                null,
                null,
                null
        );
    }

    private static class ConstraintMapper {

        private static String toConstraint(IdRev idRev) {
            return idRev.getId() + "-" + idRev.getRev();
        }

        private static IdRev toIdRev(String constraint) {
            String[] split = constraint.split("-");
            return new IdRev(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
    }

}
