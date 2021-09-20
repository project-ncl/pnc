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
package org.jboss.pnc.spi.notifications;

import java.util.Optional;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
public interface Notifier {

    void attachClient(AttachedClient attachedClient);

    void detachClient(AttachedClient attachedClient);

    int getAttachedClientsCount();

    void sendMessage(Object message);

    Optional<AttachedClient> getAttachedClient(String sessionId);

    MessageCallback getCallback();

    void onBuildStatusUpdatesSubscribe(AttachedClient client, String messagesId);

    enum Topic {
        COMPONENT_BUILD("component-build"),
        CAUSEWAY_PUSH("causeway-push"),
        BUILD_RECORDS_DELETE("build-records#delete"),
        BUILD_CONFIG_SET_RECORDS_DELETE("build-config-set-records#delete");

        Topic(String id) {
            this.id = id;
        }

        private String id;

        public String getId() {
            return id;
        }
    }
}
