/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class used to store all available build options of a BuildConfiguration or BuildConfigurationSet
 *
 * @author Jakub Bartecek
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Setter
public class BuildOptions {

    /**
     * Temporary build or standard build?
     */
    private boolean temporaryBuild = false;

    /**
     * Should we build also dependencies of this BuildConfiguration? Valid only for BuildConfiguration
     */
    private boolean buildDependencies = true;

    /**
     * Should we keep the build container running, if the build fails? Valid only for BuildConfiguration
     */
    private boolean keepPodOnFailure = false;

    /**
     * Should we add a timestamp during the alignment?
     */
    private boolean timestampAlignment = false;

    private RebuildMode rebuildMode = RebuildMode.IMPLICIT_DEPENDENCY_CHECK;

    private AlignmentPreference alignmentPreference;

    public boolean isImplicitDependenciesCheck() {
        return RebuildMode.IMPLICIT_DEPENDENCY_CHECK.equals(rebuildMode);
    }

    public boolean isForceRebuild() {
        return RebuildMode.FORCE.equals(rebuildMode);
    }

    @Deprecated
    public boolean isTimestampAlignment() {
        return false;
    }
}
