/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.task.BpmBuildConfigurationCreationTask;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.bpm.BpmBuildConfigurationCreationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmStringMapNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmTaskRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BpmTaskPage;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.*;

/**
 * This endpoint is used for starting and interacting
 * with BPM processes.
 *
 * @author Jakub Senko
 */
@Api(value = "/bpm", description = "Interaction with BPM processes.")
@Path("/bpm")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BpmEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BpmEndpoint.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BpmManager bpmManager;

    private BuildConfigurationSetProvider bcSetProvider;

    @Deprecated
    public BpmEndpoint() {
    } // CDI workaround

    @Inject
    public BpmEndpoint(BpmManager bpmManager, BuildConfigurationSetProvider bcSetProvider) {
        this.bpmManager = bpmManager;
        this.bcSetProvider = bcSetProvider;
    }


    @ApiOperation(value = "Notify PNC about a BPM task event. " +
            "Accepts polymorphic JSON {\"eventType\": \"string\"} " +
            "based on \"eventType\" field.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = "Success")
    })
    @POST
    @Path("/tasks/{taskId}/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notifyTask(
            @Context HttpServletRequest request,
            @ApiParam(value = "BPM task ID", required = true) @PathParam("taskId") int taskId) throws CoreException {

        String content;
        JsonNode node;
        try {
            content = readContent(request.getInputStream());
            node = MAPPER.readTree(content);
        } catch (IOException e) {
            throw new CoreException("Could not get JSON from request data. " +
                    "Verify it is not empty and in the correct format.", e);
        }
        if (!node.has("eventType")) {
            throw new CoreException("Request JSON does not contain required \"eventType\" field.");
        }
        String eventTypeName = node.get("eventType").asText();
        BpmEventType eventType = BpmEventType.valueOf(eventTypeName);
        BpmNotificationRest notification;
        try {
            notification = MAPPER.readValue(node.traverse(), eventType.getType());
        } catch (IOException e) {
            throw new CoreException("Could not deserialize JSON request for event type '" + eventTypeName + "' " +
                    " into '" + eventType.getType() + "'. JSON value: " + content);
        }
        LOG.debug("Received notification {} for BPM task with id {}.", notification, taskId);
        bpmManager.notify(taskId, notification);
        return Response.ok().build();
    }

    private String readContent(InputStream inputStream) throws IOException {
        try (InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
             BufferedReader reader = new BufferedReader(streamReader)) {
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            return result.toString();
        }
    }

    @ApiOperation(value = "Start BC creation task.", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = "Success")
    })
    @POST
    @Path("/tasks/start-build-configuration-creation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startBCCreationTask(
            @ApiParam(value = "Task parameters.", required = true) BpmBuildConfigurationCreationRest taskData) throws CoreException {

        LOG.debug("Received request to start BC creation: " + taskData);

        BpmBuildConfigurationCreationTask task = new BpmBuildConfigurationCreationTask(taskData);

        /**
         * Given the successful BC creation, add the BC into the BC sets.
         * This solution has been selected because if this was done in BPM process
         * there would have to be a foreach cycle and at least two REST requests
         * for each BC Set ID. The process would become too complicated.
         * Notification listeners are ideal for these kind of operations.
         */
        task.addListener(BpmEventType.BCC_CREATION_SUCCESS, x -> {
            LOG.debug("Received BPM event BCC_CREATION_SUCCESS: " + x);
            BpmStringMapNotificationRest n = (BpmStringMapNotificationRest) x;
            int bcId = -1;
            try {
                bcId = Integer.valueOf(n.getData().get("buildConfigurationId"));
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Receive notification about successful BC creation '" + n +
                        "' but the ID of the newly created BC '" + n.getData().get("buildConfigurationId") +
                        "' is not a number. It should be present under 'buildConfigurationId' key.", ex);
            }

            Set<Integer> bcSetIds = taskData.getBuildConfigurationSetIds();
            if (bcSetIds == null)
                throw new RuntimeException("Set of Build Configuration Set IDs is null. Task data: " + taskData);
            for (Integer setId : bcSetIds) {
                try {
                    bcSetProvider.addConfiguration(setId, bcId);
                } catch (ValidationException e) {
                    throw new RuntimeException("Could not add BC with ID '" + bcId +
                            "' to a BC Set with id '" + setId + "'.", e);
                }
            }
        });

        task.addListener(BpmEventType.BCC_CREATION_ERROR, x -> {
            LOG.debug("Received BPM event BCC_CREATION_ERROR: " + x);
        });
        try {
            bpmManager.startTask(task);
        } catch (CoreException e) {
            throw new CoreException("Could not start BPM task: " + task, e);
        }
        return Response.ok(task.getTaskId()).build();
    }


    @ApiOperation(value = "List of active BPM tasks.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BpmTaskPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BpmTaskPage.class),
            @ApiResponse(code = INVALID_CODE, message = INVALID_DESCRIPTION, response = ErrorResponseRest.class),
            @ApiResponse(code = SERVER_ERROR_CODE, message = SERVER_ERROR_DESCRIPTION, response = ErrorResponseRest.class)
    })
    @GET
    @Path("/tasks")
    public Response getBPMTasks(
            @ApiParam(value = PAGE_INDEX_DESCRIPTION) @QueryParam(PAGE_INDEX_QUERY_PARAM) @DefaultValue(PAGE_INDEX_DEFAULT_VALUE) int pageIndex,
            @ApiParam(value = PAGE_SIZE_DESCRIPTION) @QueryParam(PAGE_SIZE_QUERY_PARAM) @DefaultValue(PAGE_SIZE_DEFAULT_VALUE) int pageSize
    ) {

        Collection<BpmTask> tasks = bpmManager.getActiveTasks();
        int totalPages = (tasks.size() / pageSize) + (tasks.size() % pageSize == 0 ? 0 : 1);
        List<BpmTaskRest> pagedTasks = tasks.stream()
                .sorted().skip(pageIndex * pageSize).limit(pageSize)
                .map(mapTask).collect(Collectors.toList());
        return fromCollection(new CollectionInfo<>(pageIndex, pageSize, totalPages, pagedTasks));
    }

    private static Function<BpmTask, BpmTaskRest> mapTask = (BpmTask t) -> new BpmTaskRest(
            t.getTaskId(),
            t.getProcessInstanceId(),
            t.getProcessName(),
            t.getEvents());

    @GET
    @Path("/tasks/{taskId}")
    public Response getBPMTaskById(
            @ApiParam(value = "BPM task ID", required = true) @PathParam("taskId") int taskId
    ) {
        Optional<BpmTask> task = bpmManager.getTaskById(taskId);
        if (task.isPresent()) {
            return fromSingleton(mapTask.apply(task.get()));
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
