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
package org.jboss.pnc.notification;

import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.dto.Operation;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.OperationNotification;
import org.jboss.pnc.mapper.api.BuildPushOperationMapper;
import org.jboss.pnc.mapper.api.BuildPushReportMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.notification.dist.Distributed;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

/**
 * Observe build events
 */
@ApplicationScoped
public class DefaultEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventObserver.class);

    @Inject
    @Distributed
    Notifier notifier;

    @Inject
    DeliverableAnalyzerOperationRepository deliverableAnalyzerOperationRepository;

    @Inject
    BuildPushOperationRepository buildPushOperationRepository;

    @Inject
    BuildPushReportMapper buildPushReportMapper;

    @Inject
    DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;

    @Inject
    BuildPushOperationMapper buildPushOperationMapper;

    public void collectBuildStatusChangedEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        logger.trace("Observed new status changed event {}.", buildStatusChangedEvent);
        notifier.sendMessage(
                new BuildChangedNotification(
                        buildStatusChangedEvent.getOldStatus(),
                        buildStatusChangedEvent.getBuild()));
        logger.trace("Status changed event processed {}.", buildStatusChangedEvent);
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        logger.trace("Observed new set status changed event {}.", buildSetStatusChangedEvent);
        notifier.sendMessage(
                new GroupBuildChangedNotification(
                        buildSetStatusChangedEvent.getGroupBuild(),
                        buildSetStatusChangedEvent.getOldBuildStatus()));
        logger.trace("Set status changed event processed {}.", buildSetStatusChangedEvent);
    }

    public void collectOperationChangedEvent(@ObservesAsync OperationChangedEvent operationChangedEvent) {
        logger.trace("Observed new OperationChangedEvent event {}.", operationChangedEvent);
        String notificationType;
        Operation operationToSend;
        if (operationChangedEvent.getOperationClass() == DeliverableAnalyzerOperation.class) {
            notificationType = "DELIVERABLES_ANALYSIS";
            DeliverableAnalyzerOperation deliverableAnalyzerOperation = deliverableAnalyzerOperationRepository
                    .queryById(operationChangedEvent.getId());
            deliverableAnalyzerOperation.setProgressStatus(operationChangedEvent.getStatus());
            deliverableAnalyzerOperation.setResult(operationChangedEvent.getResult());
            operationToSend = deliverableAnalyzerOperationMapper.toDTO(deliverableAnalyzerOperation);
        } else if (operationChangedEvent.getOperationClass() == BuildPushOperation.class) {
            notificationType = "BUILD_PUSH";
            BuildPushOperation buildPushOperation = buildPushOperationRepository
                    .queryById(operationChangedEvent.getId());
            buildPushOperation.setProgressStatus(operationChangedEvent.getStatus());
            buildPushOperation.setResult(operationChangedEvent.getResult());
            operationToSend = buildPushOperationMapper.toDTO(buildPushOperation);
            if (buildPushOperation.getProgressStatus() == ProgressStatus.FINISHED) { // TODO: Remove in next version
                notifier.sendMessage(
                        new BuildPushResultNotification(buildPushReportMapper.fromOperation(buildPushOperation)));
            }
        } else {
            notificationType = "UNKNOWN-OPERATION";
            operationToSend = null;
        }

        notifier.sendMessage(
                new OperationNotification(
                        notificationType,
                        operationChangedEvent.getId().toString(),
                        operationChangedEvent.getStatus(),
                        operationChangedEvent.getPreviousStatus(),
                        operationChangedEvent.getResult(),
                        operationToSend));
        logger.trace("OperationChangedEvent event processed {}.", operationChangedEvent);
    }

}
