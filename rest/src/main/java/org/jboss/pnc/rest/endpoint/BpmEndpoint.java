/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.rest.provider.BuildConfigurationSetProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
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

/**
 * This endpoint is used for starting and interacting with BPM processes.
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
    public BpmEndpoint(
            BpmManager bpmManager,
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
    public Response notifyTask(@Context HttpServletRequest request, @PathParam("taskId") int taskId)
            throws CoreException {

        String content;
        JsonNode node;
        try {
            content = readContent(request.getInputStream());
            node = JsonOutputConverterMapper.getMapper().readTree(content);
        } catch (IOException e) {
            throw new CoreException(
                    "Could not get JSON from request data. " + "Verify it is not empty and in the correct format.",
                    e);
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
            throw new CoreException(
                    "Could not deserialize JSON request for event type '" + eventTypeName + "' " + " into '"
                            + eventType.getType() + "'. JSON value: " + content,
                    e);
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
}
