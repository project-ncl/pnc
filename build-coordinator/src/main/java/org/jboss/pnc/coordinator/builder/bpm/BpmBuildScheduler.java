/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.builder.bpm;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    @Deprecated
    public BpmBuildScheduler() { // CDI workaround
    }

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {
        throw new UnsupportedOperationException("Will be removed.");
    }

    @Override
    public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
        throw new UnsupportedOperationException("Only to be used with remote build scheduler.");
    }

    @Override
    public boolean cancel(BuildTask buildTask) {
        throw new UnsupportedOperationException("Will be removed.");
    }
}
