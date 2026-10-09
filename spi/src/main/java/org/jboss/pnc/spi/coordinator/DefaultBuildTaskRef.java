/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Builder(toBuilder = true, builderClassName = "Builder")
@ToString
public class DefaultBuildTaskRef implements BuildTaskRef {

    @Getter
    private final String id;

    @Getter
    private final IdRev idRev;

    @Getter
    private final Base32LongID buildConfigSetRecordId;

    @Getter
    private final ProductMilestone productMilestone;

    @Getter
    private final String contentId;

    @Getter
    private final Instant submitTime;

    @Getter
    private final Instant startTime;

    @Getter
    private final Instant endTime;

    @Getter
    private final User user;

    @Getter
    private final BuildCoordinationStatus status;

    @Getter
    private final boolean temporaryBuild;

    @Getter
    private final AlignmentPreference alignmentPreference;

    @Getter
    private final RebuildMode rebuildMode;

    @Getter
    private final BuildRecord noRebuildCause;

    @Getter
    @Builder.Default
    private final Set<String> dependants = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> dependencies = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> taskDependants = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> taskDependencies = new HashSet<>();

}
