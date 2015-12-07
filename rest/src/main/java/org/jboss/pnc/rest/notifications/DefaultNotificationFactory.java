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
package org.jboss.pnc.rest.notifications;

import org.jboss.pnc.model.*;
import org.jboss.pnc.model.event.EntityUpdateEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.pnc.spi.notifications.model.EventType.ENTITY_UPDATE_EVENT;

@ApplicationScoped
public class DefaultNotificationFactory implements NotificationFactory {

    public static final Logger logger = LoggerFactory.getLogger(DefaultNotificationFactory.class);

    private static final Map<Class<?>, String> m = new HashMap<>();

    static {
        m.put(Artifact.class, "Artifact");
        m.put(BuildConfigSetRecord.class, "BuildConfigurationSetRecord");
        m.put(BuildConfiguration.class, "BuildConfiguration");
        m.put(BuildConfigurationAudited.class, "BuildConfiguration");
        m.put(BuildConfigurationSet.class, "BuildConfigurationSet");
        m.put(BuildEnvironment.class, "BuildEnvironment");
        m.put(BuildRecord.class, "BuildRecord");
        m.put(BuildRecordSet.class, "BuildRecordSet");
        m.put(License.class, "License");
        m.put(Product.class, "Product");
        m.put(ProductMilestone.class, "ProductMilestone");
        m.put(ProductRelease.class, "ProductRelease");
        m.put(ProductVersion.class, "ProductVersion");
        m.put(Project.class, "Project");
        m.put(User.class, "Artifact");
    }

    public DefaultNotificationFactory() {
    }

    @Override
    public Notification createNotification(BuildStatusChangedEvent event) {
        BuildChangedPayload payload = new BuildChangedPayload(event.getBuildTaskId(), event.getNewStatus(), event.getBuildConfigurationId(), event.getUserId());

        return new Notification(EventType.BUILD_STATUS_CHANGED, null, payload);
    }

    @Override
    public Notification createNotification(BuildSetStatusChangedEvent event) {
        BuildSetChangedPayload payload = new BuildSetChangedPayload(event.getBuildSetTaskId(), event.getNewStatus(), event.getBuildSetConfigurationId(), event.getUserId());

        return new Notification(EventType.BUILD_SET_STATUS_CHANGED, null, payload);
    }

    @Override
    public Notification createNotification(EntityUpdateEvent event) {
        String entityClass = m.get(event.getEntityClass());
        if(entityClass != null) {
            EntityUpdatePayload payload = new EntityUpdatePayload(event.getEntityId(), null, entityClass, event.getOperationType());
            return new Notification(ENTITY_UPDATE_EVENT, null, payload);
        } else {
            logger.debug("Could not find <entity class> to <string> mapping for EntityUpdateEvent. " +
                    "Class: " + event.getEntityClass() + ". Not sending an event.");
        }
        return null;
    }
}
