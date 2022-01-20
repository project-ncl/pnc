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
package org.jboss.pnc.bpm.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = RepositoryCreationResultRest.RepositoryCreationResultRestBuilder.class)
@AllArgsConstructor
@Builder
@XmlRootElement(name = "RepositoryCreationResultRest")
@ToString
@NoArgsConstructor
@Setter
public class RepositoryCreationResultRest extends BpmEvent {

    @Override
    public String getEventType() {
        return eventType.name();
    }

    @Getter
    private Integer repositoryId;

    @Getter
    private Integer buildConfigurationId;

    @Getter
    private EventType eventType;

    @Getter
    private String errorMessage;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepositoryCreationResultRestBuilder {
    }

    public enum EventType {
        RC_CREATION_SUCCESS, RC_CREATION_ERROR
    }
}
