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
package org.jboss.pnc.termdbuilddriver.statusupdates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.termdbuilddriver.statusupdates.event.UpdateEvent;
import org.jboss.pnc.termdbuilddriver.websockets.AbstractWebSocketsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class TermdStatusUpdatesConnection extends AbstractWebSocketsConnection {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String WEB_SOCKET_TERMINAL_PATH = "/socket/process-status-updates";

    private final ObjectMapper mapper = new ObjectMapper();

    private List<Consumer<UpdateEvent>> updateConsumers = new CopyOnWriteArrayList<>();

    public TermdStatusUpdatesConnection(URI serverBaseUri) {
        super(serverBaseUri.resolve(WEB_SOCKET_TERMINAL_PATH));
    }

    @Override
    public void onTextData(String data) {
        try {
            logger.debug("Received status update notification {} ", data);
            UpdateEvent updateEvent = mapper.readValue(data, UpdateEvent.class);
            updateConsumers.forEach(consumer -> consumer.accept(updateEvent));
        } catch (IOException e) {
            new TermdMarshallingException("Could not map '" + data + "' to Object", e);
        }
    }

    public void clearConsumers() {
        updateConsumers.clear();
    }

    public void addUpdateConsumer(Consumer<UpdateEvent> consumer) {
        updateConsumers.add(consumer);
    }
}
