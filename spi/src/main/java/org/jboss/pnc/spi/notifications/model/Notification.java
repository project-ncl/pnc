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
package org.jboss.pnc.spi.notifications.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Notification {

    private final String exceptionMessage;

    private final EventType eventType;

    private final NotificationPayload payload;

    public Notification(EventType eventType, String exceptionMessage, NotificationPayload payload) {
        this.exceptionMessage = exceptionMessage;
        this.payload = payload;
        this.eventType = eventType;
    }

    public Notification(String serialized) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(serialized);
        String eventTypeString = node.get("eventType").asText();
        EventType eventType = EventType.valueOf(eventTypeString);

        BuildChangedPayload buildStatusUpdate = null;
        if (EventType.BUILD_STATUS_CHANGED.equals(eventType)) {
            buildStatusUpdate = objectMapper.convertValue(node.get("payload"), BuildChangedPayload.class);
        }

        this.exceptionMessage = null;
        this.eventType = eventType;
        this.payload = buildStatusUpdate;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public NotificationPayload getPayload() {
        return payload;
    }

    public EventType getEventType() {
        return eventType;
    }
}
