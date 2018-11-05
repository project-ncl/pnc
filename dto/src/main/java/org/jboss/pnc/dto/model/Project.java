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
package org.jboss.pnc.dto.model;

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class Project extends ProjectRef {

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private final RepositoryConfiguration repositoryConfiguration;

    private final List<BuildConfigurationRef> buildConfigurations;

    @lombok.Builder(builderClassName = "Builder")
    public Project(RepositoryConfiguration repositoryConfiguration, List<BuildConfigurationRef> buildConfigurations, Integer id, String name, String description, String issueTrackerUrl, String projectUrl) {
        super(id, name, description, issueTrackerUrl, projectUrl);
        this.repositoryConfiguration = repositoryConfiguration;
        this.buildConfigurations = buildConfigurations;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
