/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.InMemory;
import org.jboss.pnc.spi.coordinator.Remote;

@ApplicationScoped
public class BuildCoordinatorFactory {

    @Inject
    SystemConfig config;

    @Any
    @Inject
    Instance<BuildCoordinator> buildCoordinators;

    @Produces
    @Default
    @ApplicationScoped
    public BuildCoordinator init() {
        if (config.isLegacyBuildCoordinator()) {
            return buildCoordinators.select(InMemory.Literal.INSTANCE).get();
        } else {
            return buildCoordinators.select(Remote.Literal.INSTANCE).get();
        }
    }
}