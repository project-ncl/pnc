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
package org.jboss.pnc.notification.dist;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.notifications.Notifier;

public abstract class AbstractDistributedEventHandler implements DistributedEventHandler {
    private static final JacksonProvider mapperProvider = new JacksonProvider();

    Notifier notifier;

    protected String toMessage(Object event) {
        try {
            return mapperProvider.getMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert object to JSON", e);
        }
    }

    protected void sendMessage(Object json) {
        notifier.sendMessage(json);
    }
}
