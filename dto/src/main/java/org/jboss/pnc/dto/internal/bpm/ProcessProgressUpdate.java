/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dto.internal.bpm;

import org.jboss.pnc.enums.BPMTaskStatus;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonTypeName(value = ProcessProgressUpdate.PROCESS_PROGRESS_UPDATE)
@JsonDeserialize(builder = ProcessProgressUpdate.Builder.class)
public class ProcessProgressUpdate extends BPMNotification {
    static final String PROCESS_PROGRESS_UPDATE = "PROCESS_PROGRESS_UPDATE";

    /**
     * Name of the service managed by the BPM eg. Repour
     */
    private final String taskName;

    private final BPMTaskStatus bpmTaskStatus;

    /**
     * Url to subscribe to detailed notification.
     * Notifications can be a string stream of live log or an object with detailed statuses.
     */
    private final String detailedNotificationsEndpointUrl;

    @lombok.Builder(builderClassName = "Builder")
    public ProcessProgressUpdate(String taskName, BPMTaskStatus bpmTaskStatus, String detailedNotificationsEndpointUrl) {
        super(PROCESS_PROGRESS_UPDATE);
        this.taskName = taskName;
        this.bpmTaskStatus = bpmTaskStatus;
        this.detailedNotificationsEndpointUrl = detailedNotificationsEndpointUrl;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
