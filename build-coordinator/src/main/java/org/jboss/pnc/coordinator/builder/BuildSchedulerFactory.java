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

package org.jboss.pnc.coordinator.builder;

import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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
