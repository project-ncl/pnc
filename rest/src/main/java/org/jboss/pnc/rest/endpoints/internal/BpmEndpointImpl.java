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
package org.jboss.pnc.rest.endpoints.internal;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.NoEntityException;
import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.rest.endpoints.internal.api.BpmEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.jboss.pnc.bpm.BpmEventType.nullableValueOf;

@ApplicationScoped
public class BpmEndpointImpl implements BpmEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BpmEndpointImpl.class);

    @Inject
    private BpmManager bpmManager;

    @Inject
    SCMRepositoryProvider scmRepositoryProvider;

    @Inject
    private ProductMilestoneReleaseManager productMilestoneReleaseManager;

    @Context
    private HttpServletRequest request;

    @Override
    public void notifyTask(int taskId) {

        String content;
        JsonNode node;

        try {

            content = readContent(request.getInputStream());
            node = JsonOutputConverterMapper.getMapper().readTree(content);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not get JSON from request data. " + "Verify it is not empty and in the correct format.",
                    e);
        }

        if (!node.has("eventType")) {
            throw new RuntimeException("Request JSON does not contain required \"eventType\" field.");
        }

        String eventTypeName = node.get("eventType").asText();
        BpmEventType eventType = nullableValueOf(eventTypeName);
        if (eventType != null) {
            BpmEvent notification;

            try {
                notification = JsonOutputConverterMapper.getMapper().readValue(node.traverse(), eventType.getType());
            } catch (IOException e) {
                throw new RuntimeException(
                        "Could not deserialize JSON request for event type '" + eventTypeName + "' " + " into '"
                                + eventType.getType() + "'. JSON value: " + content,
                        e);
            }

            logger.debug("Received notification {} for BPM task with id {}.", notification, taskId);
            try {
                bpmManager.notify(taskId, notification);
            } catch (NoEntityException e) {
                throw new EmptyEntityException(e.getMessage());
            }
        } else {
            logger.info("Received notification with unknown eventType {}, ignoring it.", eventTypeName);
        }
    }

    @Override
    public void repositoryCreationCompleted(RepositoryCreationResult repositoryCreationResult) {
        scmRepositoryProvider.repositoryCreationCompleted(repositoryCreationResult);
    }

    @Override
    public void milestoneReleaseCompleted(MilestoneReleaseResultRest milestoneReleaseResult) {
        productMilestoneReleaseManager.productMilestoneCloseCompleted(milestoneReleaseResult);
    }

    private String readContent(InputStream inputStream) throws IOException {

        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
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
