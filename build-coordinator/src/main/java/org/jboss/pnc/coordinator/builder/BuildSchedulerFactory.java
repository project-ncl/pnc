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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.coordinator.builder.local.LocalBuildScheduler;
import org.jboss.pnc.spi.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class BuildSchedulerFactory {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_SCHEDULER_ID = LocalBuildScheduler.ID;

    private BuildScheduler configuredBuildScheduler;

    @Deprecated // CDI workaround
    public BuildSchedulerFactory() {
    }

    @Inject
    public BuildSchedulerFactory(Instance<BuildScheduler> availableSchedulers, Configuration configuration)
            throws CoreException {
        AtomicReference<String> schedulerId = new AtomicReference<>(null);
        try {
            schedulerId.set(
                    configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class)).getBuildSchedulerId());
        } catch (ConfigurationParseException e) {
            logger.warn("Unable parse config. Using default scheduler");
            schedulerId.set(DEFAULT_SCHEDULER_ID);
        }
        availableSchedulers.forEach(scheduler -> setMatchingScheduler(scheduler, schedulerId.get()));
        if (configuredBuildScheduler == null) {
            throw new CoreException(
                    "Cannot get BuildScheduler, check configurations and make sure a scheduler with configured id is available for injection. configured id: "
                            + schedulerId);
        }
    }

    private void setMatchingScheduler(BuildScheduler scheduler, String schedulerId) {
        if (scheduler.getId().equals(schedulerId)) {
            configuredBuildScheduler = scheduler;
        }
    }

    public BuildScheduler getBuildScheduler() {
        return configuredBuildScheduler;
    }
}
