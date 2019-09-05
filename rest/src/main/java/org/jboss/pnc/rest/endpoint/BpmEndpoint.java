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
package org.jboss.pnc.rest.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Hidden;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.bpm.model.RepositoryCreationProcess;
import org.jboss.pnc.bpm.model.RepositoryCreationResultRest;
import org.jboss.pnc.bpm.model.RepositoryCreationSuccess;
import org.jboss.pnc.bpm.task.RepositoryCreationTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.concurrent.MDCWrappers;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationUrlAutoRest;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.EmptyEntityException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.validators.ScmUrlValidator;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This endpoint is used for starting and interacting
 * with BPM processes.
 *
 * @author Jakub Senko
 */
@Hidden
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

    private SequenceHandlerRepository sequenceHandlerRepository;

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
            SequenceHandlerRepository sequenceHandlerRepository,
            Configuration configuration) throws ConfigurationParseException {
        this.bpmManager = bpmManager;
        this.bcSetProvider = bcSetProvider;
        this.wsNotifier = wsNotifier;
        this.authenticationProvider = authenticationProviderFactory.getProvider();
        this.repositoryConfigurationRepository = repositoryConfigurationRepository;
        this.repositoryConfigurationProvider = repositoryConfigurationProvider;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.sequenceHandlerRepository = sequenceHandlerRepository;
        this.moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
    }

    @POST
    @Path("/tasks/{taskId}/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notifyTask(
            @Context HttpServletRequest request,
            @PathParam("taskId") int taskId) throws CoreException {

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
        BpmEvent notification;
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
    private void onRCCreationSuccess(BpmEvent notification, BuildConfigurationRest buildConfigurationRest) {
        LOG.debug("Received BPM event RC_CREATION_SUCCESS: " + notification);

        RepositoryCreationSuccess repositoryCreationTaskResult = (RepositoryCreationSuccess) notification;

        int repositoryConfigurationId = repositoryCreationTaskResult.getData().getRepositoryConfigurationId();
        int buildConfigurationSavedId = -1;

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

    @POST
    @Path("/tasks/start-repository-configuration-creation-url-auto")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startRCreationTaskWithSingleUrl(
            RepositoryCreationUrlAutoRest repositoryCreationUrlAutoRest,
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

        Long buildConfigurationId = sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME);
        buildConfigurationRest.setId(buildConfigurationId.intValue());
        MDCUtils.addProcessContext(buildConfigurationId.toString());

        String revision = null;
        if (buildConfigurationRest != null) {
            revision = buildConfigurationRest.getScmRevision();
        }
        org.jboss.pnc.bpm.model.RepositoryConfiguration repositoryConfig = org.jboss.pnc.bpm.model.RepositoryConfiguration.builder()
                .externalUrl(repositoryConfigurationRest.getExternalUrl())
                .internalUrl(repositoryConfigurationRest.getInternalUrl())
                .preBuildSyncEnabled(repositoryConfigurationRest.getPreBuildSyncEnabled())
                .build();

        RepositoryCreationProcess repositoryConfigurationProcessRest = new RepositoryCreationProcess(
                repositoryConfig, revision);

        RepositoryCreationTask repositoryCreationTask = new RepositoryCreationTask(repositoryConfigurationProcessRest, loginInUser.getTokenString());

        repositoryCreationTask.addListener(BpmEventType.RC_CREATION_SUCCESS,
                MDCWrappers.wrap(x -> onRCCreationSuccess(x, buildConfigurationRest)));

        repositoryCreationTask.addListener(BpmEventType.RC_CREATION_ERROR,
                MDCWrappers.wrap(x -> LOG.debug("Received BPM event RC_CREATION_ERROR: " + x)));

        addWebsocketForwardingListeners(repositoryCreationTask);

        try {
            bpmManager.startTask(repositoryCreationTask);
        } catch (CoreException e) {
            throw new CoreException("Could not start BPM task: " + repositoryCreationTask, e);
        }
        MDCUtils.removeProcessContext();
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
        Consumer<? extends BpmEvent> doNotify = (e) -> wsNotifier.sendMessage(e);
        task.addListener(BpmEventType.RC_REPO_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CREATION_ERROR, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_ERROR, doNotify);
        //clients are notified from callback in startRCreationTask
        //task.addListener(BpmEventType.RC_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_CREATION_ERROR, doNotify);
    }
}
