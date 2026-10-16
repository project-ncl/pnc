/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildMeta {

    private String id;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    private IdRev idRev;

    private String contentId;

    private boolean temporaryBuild;

    private AlignmentPreference alignmentPreference;

    private RebuildMode rebuildMode;

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    @Past(groups = { WhenCreatingNew.class, WhenImporting.class })
    private Date submitTime;

    @NotBlank(groups = { WhenCreatingNew.class, WhenImporting.class })
    private String username;

    private Integer productMilestoneId;

    private String noRebuildCauseId;

    private List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependants;

    private List<@NotNull(groups = { WhenCreatingNew.class, WhenImporting.class }) String> dependencies;
}
