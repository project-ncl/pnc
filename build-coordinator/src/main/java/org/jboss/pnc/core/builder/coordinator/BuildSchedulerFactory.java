/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.core.builder.coordinator;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.exception.CoreException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildSchedulerFactory {

    BuildScheduler configuredBuildScheduler;

    private Configuration configuration;

    @Deprecated //CDI workaround
    public BuildSchedulerFactory() {
    }

    @Inject
    public BuildSchedulerFactory(Instance<BuildScheduler> availableSchedulers, Configuration configuration) throws ConfigurationParseException, CoreException {
        this.configuration = configuration;
        String schedulerId = configuration.getModuleConfig(new PncConfigProvider<SystemConfig>(SystemConfig.class)).getBuildSchedulerId();
        availableSchedulers.forEach(scheduler -> setMatchingScheduler(scheduler, schedulerId));
        if (configuredBuildScheduler == null) {
            throw new CoreException("Cannot get BuildScheduler, check configurations and make sure a scheduler with configured id is available for injection. configured id: " + schedulerId);
        }
    }

    private void setMatchingScheduler(BuildScheduler scheduler, String schedulerId) {
        if (scheduler.getId().equals(schedulerId)) {
            configuredBuildScheduler = scheduler;
        }
    }

    BuildScheduler getBuildScheduler() {
        return configuredBuildScheduler;
    }
}
