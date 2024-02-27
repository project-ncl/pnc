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

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.Operation;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.dto.notification.BuildPushResultNotification;
import org.jboss.pnc.dto.notification.GroupBuildChangedNotification;
import org.jboss.pnc.dto.notification.OperationNotification;
import org.jboss.pnc.dto.notification.ProductMilestoneCloseResultNotification;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.notification.dist.DistributedEventHandler;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;

/**
 * Observe build events
 */
@ApplicationScoped
public class DefaultEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    DistributedEventHandler handler;

    @Inject
    DeliverableAnalyzerOperationRepository deliverableAnalyzerOperationRepository;

    @Inject
    DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;

    private void sendMessage(Object msg) {
        handler.sendEvent(msg);
    }

    public void collectBuildPushResultEvent(@Observes BuildPushResult buildPushResult) {
        logger.trace("Observed new BuildPushResult event {}.", buildPushResult);
        sendMessage(new BuildPushResultNotification(buildPushResult));
        logger.trace("BuildPushResult event processed {}.", buildPushResult);
    }

    public void collectBuildStatusChangedEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        logger.trace("Observed new status changed event {}.", buildStatusChangedEvent);
        sendMessage(
                new BuildChangedNotification(
                        buildStatusChangedEvent.getOldStatus(),
                        buildStatusChangedEvent.getBuild()));
        logger.trace("Status changed event processed {}.", buildStatusChangedEvent);
    }

    public void collectBuildSetStatusChangedEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        logger.trace("Observed new set status changed event {}.", buildSetStatusChangedEvent);
        sendMessage(
                new GroupBuildChangedNotification(
                        buildSetStatusChangedEvent.getGroupBuild(),
                        buildSetStatusChangedEvent.getOldBuildStatus()));
        logger.trace("Set status changed event processed {}.", buildSetStatusChangedEvent);
    }

    public void collectProductMilestoneCloseResultEvent(@Observes ProductMilestoneCloseResult milestoneCloseResult) {
        logger.trace("Observed new MilestoneCloseResult event {}.", milestoneCloseResult);
        sendMessage(new ProductMilestoneCloseResultNotification(milestoneCloseResult));
        logger.trace("ProductMilestoneCloseResult event processed {}.", milestoneCloseResult);
    }

    public void collectOperationChangedEvent(@Observes OperationChangedEvent operationChangedEvent) {
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
        } else {
            notificationType = "UNKNOWN-OPERATION";
            operationToSend = null;
        }

        sendMessage(
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
