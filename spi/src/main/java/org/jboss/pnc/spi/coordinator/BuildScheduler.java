/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.spi.exception.CoreException;

/**
 * BuildScheduler is used to direct the build to by scheduler defined execution engine. Example: BuildCoordinator uses
 * BuildScheduler to start the builds and depending on BuildScheduler implementation builds can be pushed to BPM engine
 * (BpmBuildScheduler) or submitted directly (LocalBuildScheduler).
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildScheduler {

    // TODO remove after in-memory scheduling gets removed and Rex is stable
    @Deprecated
    void startBuilding(BuildTask buildTask) throws CoreException;

    void startBuilding(BuildSetTask buildSetTask) throws CoreException;

    boolean cancel(BuildTask buildTask) throws CoreException;
}
