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

import org.jboss.pnc.enums.BuildStatus;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Value;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Value
public class GroupBuild extends GroupBuildRef {

    private final GroupConfigRef groupConfig;

    private final UserRef user;

    private final ProductVersionRef productVersion;

    private final Set<Integer> buildIds;

    @lombok.Builder(builderClassName = "Builder")
    public GroupBuild(GroupConfigRef groupConfig, UserRef user, ProductVersionRef productVersion, Set<Integer> buildIds, Integer id, Instant startTime, Instant endTime, BuildStatus status, Boolean temporaryBuild) {
        super(id, startTime, endTime, status, temporaryBuild);
        this.groupConfig = groupConfig;
        this.user = user;
        this.productVersion = productVersion;
        this.buildIds = buildIds;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
