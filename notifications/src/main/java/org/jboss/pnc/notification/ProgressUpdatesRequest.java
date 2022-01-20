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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Message { message-type: 'process-updates', message: { action: 'subscribe|unsubscribe', topic: 'component-build', id:
 * 123 } }
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = ProgressUpdatesRequest.ProgressUpdatesRequestBuilder.class)
@AllArgsConstructor
@Builder
@Getter
public class ProgressUpdatesRequest { // TODO use generic name for all type of subscription based notifications

    private Action action;

    private String topic;

    private String id;

    public static ProgressUpdatesRequest subscribe(String topic, String id) {
        return new ProgressUpdatesRequest(Action.SUBSCRIBE, topic, id);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProgressUpdatesRequestBuilder {
    }

}
