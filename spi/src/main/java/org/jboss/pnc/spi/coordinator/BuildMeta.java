/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import java.util.Date;
import java.util.List;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.model.IdRev;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildMeta {
    @Getter
    String id;

    @Getter
    IdRev idRev;

    @Getter
    String contentId;

    @Getter
    boolean temporaryBuild;

    @Getter
    AlignmentPreference alignmentPreference;

    @Getter
    RebuildMode rebuildMode;

    @Getter
    Date submitTime;

    @Getter
    String username;

    @Getter
    Integer productMilestoneId;

    @Getter
    String noRebuildCauseId;

    @Getter
    List<String> dependants;

    @Getter
    List<String> dependencies;
}
