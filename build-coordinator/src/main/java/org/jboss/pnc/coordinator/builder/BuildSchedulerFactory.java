/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.builder;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildSchedulerFactory {

    private static final Logger logger = LoggerFactory.getLogger(BuildSchedulerFactory.class);

    private BuildScheduler configuredBuildScheduler;

    @Deprecated // CDI workaround
    public BuildSchedulerFactory() {
    }

    @Inject
    public BuildSchedulerFactory(BuildScheduler buildScheduler) throws CoreException {

        configuredBuildScheduler = buildScheduler;

        if (configuredBuildScheduler == null) {
            throw new CoreException("Cannot get BuildScheduler");
        }
    }

    public BuildScheduler getBuildScheduler() {
        return configuredBuildScheduler;
    }
}
