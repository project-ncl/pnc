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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.Instant;

/**
 * The build.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = BuildRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildRef implements DTOEntity {

    /**
     * ID of the build.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    /**
     * The thime when the build was submited for building.
     */
    protected final Instant submitTime;

    /**
     * The time when the build started building.
     */
    protected final Instant startTime;

    /**
     * The time when the build finished building.
     */
    protected final Instant endTime;

    /**
     * Higl level progress status of the build. This indicate if the build is waiting, in progress or finished.
     */
    protected final BuildProgress progress;

    /**
     * The status of the build.
     */
    protected final BuildStatus status;

    /**
     * The identifier to use when accessing repository or other content stored via external services.
     */
    protected final String buildContentId;

    /**
     * Whether the build is temporary or not.
     */
    protected final Boolean temporaryBuild;

    /**
     * AlignmentPreference that was used for the build.
     */
    protected final AlignmentPreference alignmentPreference;

    /**
     * Url to the SCM repository with the sources being built.
     */
    protected final String scmUrl;

    /**
     * The revision number in the SCM repository of the sources being built.
     */
    protected final String scmRevision;

    /**
     * The tag in the SCM repository that was built.
     */
    protected final String scmTag;

    /**
     * Checksum of build logs. Used to verify the integrity of the logs in the remote storage eg. Elasticsearch.
     */
    protected final String buildOutputChecksum;

    /**
     * The time when the build was inserted or last updated
     */
    protected final Instant lastUpdateTime;

    /**
     * The commit ID resolved from the build configuration revision when cloning the SCM repository, before any
     * alignment operation is made. Called Pre-alignment SCM Revision in the UI.
     */
    protected final String scmBuildConfigRevision;

    /**
     * Whether the build configuration revision was only found in the downstream (internal) repository and not upstream.
     */
    protected final Boolean scmBuildConfigRevisionInternal;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
