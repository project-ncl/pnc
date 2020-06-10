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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.util.Map;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * A PNC project is something that can be thought of as an upstream (or internal) scm repository (e.g. GitHub).
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Project.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends ProjectRef {

    /**
     * List of build configs in this project.
     */
    @PatchSupport({ ADD, REPLACE })
    private final Map<String, BuildConfigurationRef> buildConfigs;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Project(
            Map<String, BuildConfigurationRef> buildConfigs,
            String id,
            String name,
            String description,
            String issueTrackerUrl,
            String projectUrl,
            String engineeringTeam,
            String technicalLeader) {
        super(id, name, description, issueTrackerUrl, projectUrl, engineeringTeam, technicalLeader);
        this.buildConfigs = buildConfigs;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
