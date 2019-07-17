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
package org.jboss.pnc.dto;

import org.jboss.pnc.enums.BuildStatus;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = GroupBuild.Builder.class)
public class GroupBuild extends GroupBuildRef {

    private final GroupConfigurationRef groupConfig;

    private final User user;

    private final ProductVersionRef productVersion;

    private final Set<Integer> buildIds;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private GroupBuild(GroupConfigurationRef groupConfig, User user, ProductVersionRef productVersion, Set<Integer> buildIds, String id, Instant startTime, Instant endTime, BuildStatus status, Boolean temporaryBuild) {
        super(id.toString(), startTime, endTime, status, temporaryBuild);
        this.groupConfig = groupConfig;
        this.user = user;
        this.productVersion = productVersion;
        this.buildIds = buildIds;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
