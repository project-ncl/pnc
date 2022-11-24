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
package org.jboss.pnc.facade;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.DefaultBuildCoordinator;
import org.jboss.pnc.spi.coordinator.RemoteBuildCoordinator;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

public class BuildCoordinatorFactory {

    @Inject
    SystemConfig config;

    @Inject
    Instance<BuildCoordinator> buildCoordinators;

    @Produces
    public BuildCoordinator createInstance() {
        if (config.isLegacyBuildCoordinator()) {
            return buildCoordinators.select(DefaultLiteral.INSTANCE).get();
        } else {
            return buildCoordinators.select(RemoteLiteral.INSTANCE).get();
        }
    }

    public static final class DefaultLiteral extends AnnotationLiteral<DefaultBuildCoordinator> implements DefaultBuildCoordinator {
        public static final DefaultLiteral INSTANCE = new DefaultLiteral();
        private static final long serialVersionUID = 1L;
    }

    public static final class RemoteLiteral extends AnnotationLiteral<RemoteBuildCoordinator> implements RemoteBuildCoordinator {
        public static final RemoteLiteral INSTANCE = new RemoteLiteral();
        private static final long serialVersionUID = 1L;
    }
}