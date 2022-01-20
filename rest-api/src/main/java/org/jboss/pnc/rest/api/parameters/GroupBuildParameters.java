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
package org.jboss.pnc.rest.api.parameters;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.rest.configuration.SwaggerConstants;
import org.jboss.pnc.rest.validation.GroupBuildParametersConstraint;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

/**
 * This class represents a set of options of how a group build should be executed.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@GroupBuildParametersConstraint
public class GroupBuildParameters {

    /**
     * {@value SwaggerConstants#TEMPORARY_BUILD_DESC} Defaults to false.
     */
    @Parameter(description = SwaggerConstants.TEMPORARY_BUILD_DESC)
    @QueryParam("temporaryBuild")
    @DefaultValue("false")
    boolean temporaryBuild;

    /**
     * {@value SwaggerConstants#REBUILD_MODE_DESC} Defaults to {@value SwaggerConstants#DEFAULT_REBUILD_MODE}.
     * 
     * @see org.jboss.pnc.enums.RebuildMode
     */
    @Parameter(description = SwaggerConstants.DEFAULT_REBUILD_MODE)
    @QueryParam("rebuildMode")
    @DefaultValue(SwaggerConstants.DEFAULT_REBUILD_MODE)
    RebuildMode rebuildMode;

    /**
     * {@value SwaggerConstants#TIMESTAMP_ALIGNMENT_DESC} Defaults to false.
     */
    @Parameter(description = SwaggerConstants.TIMESTAMP_ALIGNMENT_DESC)
    @QueryParam("timestampAlignment")
    @DefaultValue("false")
    boolean timestampAlignment;

    /**
     * {@value SwaggerConstants#ALIGNMENT_PREFERENCE_DESC}.
     */
    @Parameter(description = SwaggerConstants.ALIGNMENT_PREFERENCE_DESC)
    @QueryParam("alignmentPreference")
    AlignmentPreference alignmentPreference;

    /**
     * AlignmentPreference defaults to PREFER_TEMPORARY for temporary build.
     */
    public AlignmentPreference getAlignmentPreference() {
        if (alignmentPreference == null && isTemporaryBuild()) {
            return AlignmentPreference.PREFER_TEMPORARY;
        } else {
            return alignmentPreference;
        }
    }
}
