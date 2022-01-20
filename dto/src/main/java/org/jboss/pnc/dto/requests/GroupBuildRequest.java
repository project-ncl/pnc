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
package org.jboss.pnc.dto.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Request to start build of a group config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = GroupBuildRequest.Builder.class)
public class GroupBuildRequest {

    /**
     * List of group config revisions overrides to be used for builds. Normally the build of group config will start
     * building all the build configs in the group in their latest revision. This list can be used to override this
     * behaviour and specify which revisions to build exactly. All the revisions should be of build configs in the
     * group, but not all build configs from the group must have specified revision (latest will be used).
     */
    private final List<BuildConfigurationRevisionRef> buildConfigurationRevisions;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
