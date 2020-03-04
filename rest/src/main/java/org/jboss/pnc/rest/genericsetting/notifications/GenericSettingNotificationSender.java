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
package org.jboss.pnc.rest.genericsetting.notifications;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.rest.restmodel.genericsetting.GenericSettingUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dustin Kut Moy Cheung <dcheung@redhat.com>
 */
@ApplicationScoped
public class GenericSettingNotificationSender {

    private Logger logger = LoggerFactory.getLogger(GenericSettingNotificationSender.class);

    private ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    public void send(GenericSettingUpdate genericSettingUpdate) {
        sessions.forEach((id, session) -> {
            session.getAsyncRemote().sendText(JsonOutputConverterMapper.apply(genericSettingUpdate), sendHandler);
        });
    }

    private SendHandler sendHandler = new SendHandler() {
        @Override
        public void onResult(SendResult result) {
            if (!result.isOK()) {
                logger.warn("Notification client threw an error, removing it. ", result.getException());
            }
        }
    };

    ConcurrentMap<String, Session> getSessions() {
        return sessions;
    }
}
