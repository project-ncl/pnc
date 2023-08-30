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
package org.jboss.pnc.facade.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.constants.HttpHeaders;
import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.OperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.OperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.slf4j.MDC;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class OperationsManagerImpl implements OperationsManager {
    private String callbackUrlTemplate = "%s/operations/%s/complete";
    @Inject
    private OperationRepository repository;
    @Inject
    private ProductMilestoneRepository productMilestoneRepository;
    @Inject
    private UserService userService;

    @Inject
    private GlobalModuleGroup globalConfig;
    @Inject
    private Event<OperationChangedEvent> analysisStatusChangedEventNotifier;
    @Inject
    private OperationsManagerImpl self;

    @Override
    public Operation updateProgress(Base32LongID id, ProgressStatus status) {
        Tuple tuple = self._updateProgress(id, status);
        analysisStatusChangedEventNotifier.fire(new OperationChangedEventImpl(tuple.operation, tuple.previousProgress));
        return tuple.operation;
    }

    @Transactional
    // must be public (and not protected), otherwise Weld doesn't know how to create proxy properly
    public Tuple _updateProgress(Base32LongID id, ProgressStatus status) {
        Operation operation = repository.queryById(id);
        if (operation.getEndTime() != null) {
            throw new InvalidEntityException("Operation " + operation + " is already finished!");
        }
        log.debug("Updating progress of operation " + operation + " to " + status);
        if (operation.getStartTime() == null && status == ProgressStatus.IN_PROGRESS) {
            operation.setStartTime(Date.from(Instant.now()));
        }
        ProgressStatus previousProgress = operation.getProgressStatus();
        operation.setProgressStatus(status);
        return new Tuple(operation, previousProgress);
    }

    @Override
    public Operation setResult(Base32LongID id, OperationResult result) {
        Tuple tuple = self._setResult(id, result);
        analysisStatusChangedEventNotifier.fire(new OperationChangedEventImpl(tuple.operation, tuple.previousProgress));
        return tuple.operation;
    }

    @Transactional
    // must be public (and not protected), otherwise Weld doesn't know how to create proxy properly
    public Tuple _setResult(Base32LongID id, OperationResult result) {
        Operation operation = repository.queryById(id);
        if (operation.getEndTime() != null) {
            throw new InvalidEntityException("Operation " + operation + " is already finished!");
        }
        log.debug("Updating result of operation " + operation + " to " + result);
        ProgressStatus previousProgress = operation.getProgressStatus();
        operation.setResult(result);
        operation.setEndTime(Date.from(Instant.now()));
        return new Tuple(operation, previousProgress);
    }

    @Override
    public DeliverableAnalyzerOperation newDeliverableAnalyzerOperation(
            String milestoneId,
            Map<String, String> inputParams) {
        ProductMilestone milestone = productMilestoneRepository
                .queryById(ProductMilestoneMapper.idMapper.toEntity(milestoneId));
        if (milestone == null) {
            throw new EmptyEntityException("Milestone with id " + milestoneId + " doesn't exist");
        }

        String operationId = Sequence.nextBase32Id();
        MDCUtils.addProcessContext(operationId);
        org.jboss.pnc.model.DeliverableAnalyzerOperation operation = org.jboss.pnc.model.DeliverableAnalyzerOperation.Builder
                .newBuilder()
                .progressStatus(ProgressStatus.NEW)
                .submitTime(Date.from(Instant.now()))
                .productMilestone(milestone)
                .operationParameters(inputParams)
                .user(userService.currentUser())
                .id(operationId)
                .build();
        operation = self.saveToDb(operation);
        analysisStatusChangedEventNotifier.fire(new OperationChangedEventImpl(operation, null));
        return operation;
    }

    @Transactional
    // must be public (and not protected), otherwise Weld doesn't know how to create proxy properly
    public <T extends Operation> T saveToDb(T operation) {
        return (T) repository.save(operation);
    }

    @Override
    public Request getOperationCallback(Base32LongID operationId) {
        List<Request.Header> headers = new ArrayList<>();
        addCommonHeaders(headers);
        addMDCHeaders(headers);
        addOTELHeaders(headers);

        String actualEndpoint = String.format(callbackUrlTemplate, globalConfig.getPncUrl(), operationId.getId());
        URI callbackURI = URI.create(actualEndpoint);
        return new Request(Request.Method.POST, callbackURI, headers);
    }

    private void addMDCHeaders(List<Request.Header> headers) {
        headersFromMdc(headers, MDCHeaderKeys.REQUEST_CONTEXT);
        headersFromMdc(headers, MDCHeaderKeys.PROCESS_CONTEXT);
        headersFromMdc(headers, MDCHeaderKeys.SLF4J_TRACE_ID);
        headersFromMdc(headers, MDCHeaderKeys.SLF4J_SPAN_ID);
    }

    private void addCommonHeaders(List<Request.Header> headers) {
        headers.add(new Request.Header(HttpHeaders.CONTENT_TYPE_STRING, MediaType.APPLICATION_JSON));
    }

    private void addOTELHeaders(List<Request.Header> headers) {

        Map<String, String> otelHeaders = MDCUtils.getOtelHeadersFromMDC();
        otelHeaders.forEach((key, value) -> {
            log.debug("Setting {}: {}", key, value);
            headers.add(new Request.Header(key, value));
        });
    }

    private void headersFromMdc(List<Request.Header> headers, MDCHeaderKeys headerKey) {
        String mdcValue = MDC.get(headerKey.getMdcKey());
        if (mdcValue != null && !mdcValue.isEmpty()) {
            headers.add(new Request.Header(headerKey.getHeaderName(), mdcValue.trim()));
        }
    }

    private Base32LongID parseId(String operationId) {
        return OperationMapper.idMapper.toEntity(operationId);
    }

    @AllArgsConstructor
    // must be public (and not protected), otherwise Weld doesn't know how to create proxy properly
    public static class Tuple {
        private final Operation operation;
        private final ProgressStatus previousProgress;
    }
}
