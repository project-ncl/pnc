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
import lombok.Data;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildPushStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * Result of a build push operation.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildPushResult.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildPushResult extends BuildPushResultRef {

    /**
     * Product milestone close result this build push is a part of.
     */
    private final ProductMilestoneCloseResultRef productMilestoneCloseResult;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public BuildPushResult(
            @NotNull(groups = WhenUpdating.class) @Null(groups = WhenCreatingNew.class) String id,
            @NotNull String buildId,
            @NotNull BuildPushStatus status,
            Integer brewBuildId,
            String brewBuildUrl,
            String logContext,
            String message,
            ProductMilestoneCloseResultRef productMilestoneCloseResult) {
        super(id, buildId, status, brewBuildId, brewBuildUrl, logContext, message);
        this.productMilestoneCloseResult = productMilestoneCloseResult;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
