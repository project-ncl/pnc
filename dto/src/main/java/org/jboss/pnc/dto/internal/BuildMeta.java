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
package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import java.util.Date;
import java.util.List;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildMeta {

    @NotNull(groups = WhenCreatingNew.class)
    @Null(groups = WhenImporting.class)
    String id;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    IdRev idRev;

    String contentId;

    boolean temporaryBuild;

    AlignmentPreference alignmentPreference;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    @Past(groups = { WhenCreatingNew.class, WhenImporting.class })
    Date submitTime;

    @NotBlank(groups = { WhenCreatingNew.class, WhenImporting.class })
    String username;

    Integer productMilestoneId;

    String noRebuildCauseId;

    List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependants;

    List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependencies;
}
