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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.time.Instant;
import java.util.Map;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * The build.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Build.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Build extends BuildRef {

    /**
     * Project of the build.
     */
    private final ProjectRef project;

    /**
     * SCM Repository that was used for the build.
     */
    private final SCMRepository scmRepository;

    /**
     * Build environment used by the build.
     */
    private final Environment environment;

    /**
     * Map of build attributes.
     */
    @PatchSupport({ ADD, REMOVE, REPLACE })
    private final Map<String, String> attributes;

    /**
     * The user who built this build.
     */
    private final User user;

    /**
     * The revision of build configuration that was used for this build.
     */
    private final BuildConfigurationRevisionRef buildConfigRevision;

    /**
     * Product mileston for which this build was created.
     */
    private final ProductMilestoneRef productMilestone;

    /**
     * Group build in which this build was built.
     */
    private final GroupBuildRef groupBuild;

    /**
     * In case of status NO_REBUILD_REQUIRED, this field references the Build that caused the decision of not
     * rebuilding.
     */
    private final BuildRef noRebuildCause;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Build(
            ProjectRef project,
            SCMRepository scmRepository,
            Environment environment,
            Map<String, String> attributes,
            User user,
            BuildConfigurationRevisionRef buildConfigRevision,
            String id,
            Instant submitTime,
            Instant startTime,
            Instant endTime,
            BuildProgress progress,
            BuildStatus status,
            String buildContentId,
            Boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            String scmUrl,
            String scmRevision,
            String scmTag,
            GroupBuildRef groupBuild,
            ProductMilestoneRef productMilestone,
            String buildOutputChecksum,
            BuildRef noRebuildCause,
            Instant lastUpdateTime) {
        super(
                id,
                submitTime,
                startTime,
                endTime,
                progress,
                status,
                buildContentId,
                temporaryBuild,
                alignmentPreference,
                scmUrl,
                scmRevision,
                scmTag,
                buildOutputChecksum,
                lastUpdateTime);
        this.project = project;
        this.scmRepository = scmRepository;
        this.environment = environment;
        this.attributes = attributes;
        this.user = user;
        this.buildConfigRevision = buildConfigRevision;
        this.productMilestone = productMilestone;
        this.groupBuild = groupBuild;
        this.noRebuildCause = noRebuildCause;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
