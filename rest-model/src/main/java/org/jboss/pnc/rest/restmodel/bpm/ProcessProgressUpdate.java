/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.restmodel.bpm;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.pnc.enums.BPMTaskStatus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = ProcessProgressUpdate.ProcessProgressUpdateBuilder.class)
@AllArgsConstructor
@Builder
@XmlRootElement
@ToString
@NoArgsConstructor
@Setter(onMethod=@__({@Deprecated}))
public class ProcessProgressUpdate extends BpmNotificationRest {

    /**
     * Name of the service managed by the BPM eg. Repour
     */
    @Getter
    private String taskName;

    @Getter
    private BPMTaskStatus bpmTaskStatus;

    /**
     * Url to subscribe to detailed notification.
     * Notifications can be a string stream of live log or an object with detailed statuses.
     */
    @Getter
    private String detailedNotificationsEndpointUrl;

    @Override
    public String getEventType() {
        return "PROCESS_PROGRESS_UPDATE";
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProcessProgressUpdateBuilder {
    }

}
