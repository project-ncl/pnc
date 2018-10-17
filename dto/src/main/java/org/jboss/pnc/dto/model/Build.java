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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
public class Build extends BuildRef {

    private final ProjectRef project;

    private final RepositoryConfigurationRef repository;

    private final BuildEnvironmentRef buildEnvironmentId;

    private final Map<String, String> attributes;

    private final GroupBuildRef groupBuild;

    /**
     * The IDs of the build record sets which represent the builds performed for a milestone to which this build record belongs
     */
    private final ProductMilestoneRef productMilestone;

    private final UserRef user;

    private final BuildConfigurationRevisionRef buildConfigurationAudited;

    private final List<Integer> dependentBuildIds;

    private final List<Integer> dependencyBuildIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
