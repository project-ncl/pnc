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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * A PNC project is something that can be thought of as an upstream (or internal) scm repository (e.g. GitHub).
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProjectRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRef implements DTOEntity {
    /**
     * ID of the project.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    /**
     * Project name. Typically in the form ${organization}/${repository}.
     */
    @PatchSupport({ REPLACE })
    protected final String name;

    /**
     * Project description.
     */
    @PatchSupport({ REPLACE })
    protected final String description;

    /**
     * URL of the issue tracker for the project.
     */
    @PatchSupport({ REPLACE })
    protected final String issueTrackerUrl;

    /**
     * URL of the project.
     */
    @PatchSupport({ REPLACE })
    protected final String projectUrl;

    /**
     * The engineering team in charge of the project.
     */
    @PatchSupport({ REPLACE })
    protected final String engineeringTeam;

    /**
     * The technical leader of the project.
     */
    @PatchSupport({ REPLACE })
    protected final String technicalLeader;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
