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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.task.RepositoryCreationTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmStringMapNotificationRest;
import org.jboss.pnc.rest.restmodel.bpm.BpmTaskRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationProcessRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationResultRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationUrlAutoRest;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.restmodel.response.error.ErrorResponseRest;
import org.jboss.pnc.rest.swagger.response.BpmTaskRestPage;
import org.jboss.pnc.rest.swagger.response.BpmTaskRestSingleton;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.validators.ScmUrlValidator;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.INVALID_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NOT_FOUND_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.NO_CONTENT_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_INDEX_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DEFAULT_VALUE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.PAGE_SIZE_QUERY_PARAM;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SERVER_ERROR_DESCRIPTION;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_CODE;
import static org.jboss.pnc.rest.configuration.SwaggerConstants.SUCCESS_DESCRIPTION;

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

    private BpmManager bpmManager;

    private BuildConfigurationSetProvider bcSetProvider;

    private Notifier wsNotifier;

    private AuthenticationProvider authenticationProvider;

    private RepositoryConfigurationRepository repositoryConfigurationRepository;
    private RepositoryConfigurationProvider repositoryConfigurationProvider;
    private BuildConfigurationRepository buildConfigurationRepository;

    private ScmModuleConfig moduleConfig;

    @Deprecated
    public BpmEndpoint() {
    } // CDI workaround

    @Inject
    public BpmEndpoint(BpmManager bpmManager,
            BuildConfigurationSetProvider bcSetProvider,
            AuthenticationProviderFactory authenticationProviderFactory,
            Notifier wsNotifier,
            RepositoryConfigurationRepository repositoryConfigurationRepository,
            RepositoryConfigurationProvider repositoryConfigurationProvider,
            BuildConfigurationRepository buildConfigurationRepository,
            Configuration configuration) throws ConfigurationParseException {
        this.bpmManager = bpmManager;
        this.bcSetProvider = bcSetProvider;
        this.wsNotifier = wsNotifier;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
        this.repositoryConfigurationRepository = repositoryConfigurationRepository;
        this.repositoryConfigurationProvider = repositoryConfigurationProvider;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
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
            node = JsonOutputConverterMapper.getMapper().readTree(content);
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
            notification = JsonOutputConverterMapper.getMapper().readValue(node.traverse(), eventType.getType());
        } catch (IOException e) {
            throw new CoreException("Could not deserialize JSON request for event type '" + eventTypeName + "' " +
                    " into '" + eventType.getType() + "'. JSON value: " + content, e);
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

    /**
     * Given the successful BC creation, add the BC into the BC sets. This solution has been
     * selected because if this was done in BPM process there would have to be a foreach cycle and
     * at least two REST requests for each BC Set ID. The process would become too complicated.
     * Notification listeners are ideal for these kind of operations.
     */
    private void onRCCreationSuccess(BpmNotificationRest notification, BuildConfigurationRest buildConfigurationRest) {
        LOG.debug("Received BPM event RC_CREATION_SUCCESS: " + notification);

        BpmStringMapNotificationRest repositoryCreationTaskResult = (BpmStringMapNotificationRest) notification;

        int repositoryConfigurationId = -1;
        int buildConfigurationSavedId = -1;
        try {
            repositoryConfigurationId = Integer.valueOf(repositoryCreationTaskResult.getData().get("repositoryConfigurationId"));
        } catch (NumberFormatException ex) {
            String errorMessage = "Receive notification about successful BC creation '" + repositoryCreationTaskResult
                    + "' but the ID of the newly created RC '" + repositoryCreationTaskResult.getData()
                    .get("repositoryConfigurationId")
                    + "' is not a number. It should be present under 'repositoryConfigurationId' key.";

            LOG.error(errorMessage, ex);
            sendErrorMessage(repositoryConfigurationId, buildConfigurationSavedId, errorMessage);
            return;
        }

        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryById(repositoryConfigurationId);
        if (repositoryConfiguration == null) {
            String errorMessage = "Repository Configuration was not found in database.";
            LOG.error(errorMessage);
            sendErrorMessage(repositoryConfigurationId, buildConfigurationSavedId, errorMessage);
            return;
        }

        if (buildConfigurationRest != null) { //TODO test me
            BuildConfiguration buildConfiguration = buildConfigurationRest.toDBEntityBuilder()
                    .repositoryConfiguration(repositoryConfiguration)
                    .build();
            BuildConfiguration buildConfigurationSaved = buildConfigurationRepository.save(buildConfiguration);
            buildConfigurationSavedId = buildConfigurationSaved.getId();

            Set<Integer> bcSetIds = buildConfigurationRest.getBuildConfigurationSetIds();
            try {
                if (bcSetIds != null) {
                    addBuildConfigurationToSet(buildConfigurationSaved, bcSetIds);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
                sendErrorMessage(repositoryConfigurationId, buildConfigurationSavedId, e.getMessage());
                return;
            }
        }

        RepositoryCreationResultRest repositoryCreationResultRest
                = new RepositoryCreationResultRest(
                        repositoryConfigurationId,
                        buildConfigurationSavedId,
                        RepositoryCreationResultRest.EventType.RC_CREATION_SUCCESS,
                        null);

        wsNotifier.sendMessage(repositoryCreationResultRest); //TODO test me!
    }

    private void sendErrorMessage(int repositoryConfigurationId, int buildConfigurationId, String message) {
        RepositoryCreationResultRest repositoryCreationResultRest =
                new RepositoryCreationResultRest(
                        repositoryConfigurationId,
                        buildConfigurationId,
                        RepositoryCreationResultRest.EventType.RC_CREATION_ERROR,
                        message);
        wsNotifier.sendMessage(repositoryCreationResultRest); //TODO test me!
    }

    private Response checkIfInternalUrlExits(String internalUrl) throws InvalidEntityException {
        if (internalUrl != null) {
            RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryByInternalScm(internalUrl);
            if (repositoryConfiguration != null) {
                String message = "{ \"repositoryConfigurationId\" : " + repositoryConfiguration.getId() + "}";
                return Response.status(Response.Status.CONFLICT).entity(message).build();
            }
        }
        return null;
    }

    private Response checkIfExternalUrlExits(String externalUrl) throws InvalidEntityException {
        if (externalUrl != null) {
            RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryByExternalScm(externalUrl);
            if (repositoryConfiguration != null) {
                String message = "{ \"repositoryConfigurationId\" : " + repositoryConfiguration.getId() + "}";
                return Response.status(Response.Status.CONFLICT).entity(message).build();
            }
        }
        return null;
    }

    @ApiOperation(value = "Start Repository Creation task with url autodetect (internal vs. external).", response = Singleton.class)
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = "Success", response = Integer.class)
    })
    @POST
    @Path("/tasks/start-repository-configuration-creation-url-auto")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startRCreationTaskWithSingleUrl(
            @ApiParam(value = "Task parameters.", required = true) RepositoryCreationUrlAutoRest repositoryCreationUrlAutoRest,
            @Context HttpServletRequest httpServletRequest) throws CoreException, InvalidEntityException, EmptyEntityException {

        LOG.debug("Received request to start RC creation with url autodetect: " + repositoryCreationUrlAutoRest);
        final BuildConfigurationRest buildConfigurationRest = repositoryCreationUrlAutoRest.getBuildConfigurationRest();

        ValidationBuilder.validateObject(repositoryCreationUrlAutoRest, WhenCreatingNew.class)
                .validateAnnotations();
        ValidationBuilder.validateObject(buildConfigurationRest, WhenCreatingNew.class)
                .validateAnnotations();

        RepositoryConfigurationRest.RepositoryConfigurationRestBuilder repositoryConfigurationBuilder = RepositoryConfigurationRest.builder();

        String internalScmAuthority = moduleConfig.getInternalScmAuthority();
        String scmUrl = repositoryCreationUrlAutoRest.getScmUrl();
        if (!ScmUrlValidator.isValid(scmUrl)) {
            throw new InvalidEntityException("Invalid scmUrl: " + scmUrl);
        }
        Boolean isUrlInternal = scmUrl.contains(internalScmAuthority);

        if (isUrlInternal) {
            repositoryConfigurationProvider.validateInternalRepository(scmUrl);
            Response message = checkIfInternalUrlExits(scmUrl);
            if (message != null) {
                return message;
            }
            repositoryConfigurationBuilder.internalUrl(scmUrl);
        } else {
            Response message = checkIfExternalUrlExits(scmUrl);
            if (message != null) {
                return message;
            }
            //when creating new SCM config with external Url, enable preBuildSync if it is not specified
            if (repositoryCreationUrlAutoRest.getPreBuildSyncEnabled() != null) {
                repositoryConfigurationBuilder.preBuildSyncEnabled(repositoryCreationUrlAutoRest.getPreBuildSyncEnabled());
            } else {
                repositoryConfigurationBuilder.preBuildSyncEnabled(true);
            }
            repositoryConfigurationBuilder.externalUrl(scmUrl);
        }

        RepositoryConfigurationRest repositoryConfigurationRest = repositoryConfigurationBuilder.build();
        repositoryConfigurationRest.validate();

        RepositoryCreationTask task = startRCreationTask(repositoryConfigurationRest, buildConfigurationRest, httpServletRequest);
        return Response.ok(task.getTaskId()).build();
    }

    private RepositoryCreationTask startRCreationTask(RepositoryConfigurationRest repositoryConfigurationRest, BuildConfigurationRest buildConfigurationRest,
            HttpServletRequest httpServletRequest) throws CoreException, InvalidEntityException, EmptyEntityException {
        LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);

        RepositoryCreationProcessRest repositoryConfigurationProcessRest = new RepositoryCreationProcessRest(
                repositoryConfigurationRest);

        RepositoryCreationTask repositoryCreationTask = new RepositoryCreationTask(repositoryConfigurationProcessRest, loginInUser.getTokenString());

        repositoryCreationTask.addListener(BpmEventType.RC_CREATION_SUCCESS,
                x -> onRCCreationSuccess(x, buildConfigurationRest));

        repositoryCreationTask.addListener(BpmEventType.RC_CREATION_ERROR, x -> {
            LOG.debug("Received BPM event RC_CREATION_ERROR: " + x);
        });

        addWebsocketForwardingListeners(repositoryCreationTask);

        try {
            bpmManager.startTask(repositoryCreationTask);
        } catch (CoreException e) {
            throw new CoreException("Could not start BPM task: " + repositoryCreationTask, e);
        }
        return repositoryCreationTask;
    }

    private void addBuildConfigurationToSet(BuildConfiguration buildConfiguration, Set<Integer> bcSetIds) throws Exception {
        for (Integer setId : bcSetIds) {
            try {
                bcSetProvider.addConfiguration(setId, buildConfiguration.getId());
            } catch (RestValidationException e) {
                throw new Exception("Could not add BC with ID '" + buildConfiguration.getId() +
                        "' to a BC Set with id '" + setId + "'.", e);
            }
        }
    }

    /**
     * This method will add listeners to all important RCC event types
     * and forward the event to WS clients.
     */
    private void addWebsocketForwardingListeners(RepositoryCreationTask task) {
        Consumer<? extends BpmNotificationRest> doNotify = (e) -> wsNotifier.sendMessage(e);
        task.addListener(BpmEventType.RC_REPO_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CREATION_ERROR, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_ERROR, doNotify);
        //clients are notified from callback in startRCreationTask
        //task.addListener(BpmEventType.RC_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_CREATION_ERROR, doNotify);
    }

    @ApiOperation(value = "List of (recently) active BPM tasks.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BpmTaskRestPage.class),
            @ApiResponse(code = NO_CONTENT_CODE, message = NO_CONTENT_DESCRIPTION, response = BpmTaskRestPage.class),
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

    @ApiOperation(value = "Get single (recently) active BPM task.")
    @ApiResponses(value = {
            @ApiResponse(code = SUCCESS_CODE, message = SUCCESS_DESCRIPTION, response = BpmTaskRestSingleton.class),
            @ApiResponse(code = NOT_FOUND_CODE, message = NOT_FOUND_DESCRIPTION, response = BpmTaskRestSingleton.class),
    })
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
