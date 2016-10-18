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

package org.jboss.pnc.environment.openshift;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.environment.monitor.PullingMonitor;
import org.jboss.pnc.common.util.NamedThreadFactory;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class OpenshiftEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftEnvironmentDriver.class);

    public static List<SystemImageType> compatibleImageTypes = Arrays.asList(SystemImageType.DOCKER_IMAGE);

    //TODO configurable:
    private ExecutorService executor = Executors.newFixedThreadPool(4, new NamedThreadFactory("openshift-environment-driver"));

    private OpenshiftEnvironmentDriverModuleConfig config;
    private PullingMonitor pullingMonitor;

    @Deprecated //CDI workaround
    public OpenshiftEnvironmentDriver() {
    }

    @Inject
    public OpenshiftEnvironmentDriver(Configuration configuration, PullingMonitor pullingMonitor) throws ConfigurationParseException {
        this.pullingMonitor = pullingMonitor;
        config = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftEnvironmentDriverModuleConfig.class));

        logger.info("Is OpenShift environment driver disabled: {}", config.isDisabled());
    }

    @Override
    public StartedEnvironment startEnvironment(String systemImageId,
                                               String systemImageRepositoryUrl,
                                               SystemImageType systemImageType,
                                               RepositorySession repositorySession,
                                               DebugData debugData) throws EnvironmentDriverException {
        if (!canRunImageType(systemImageType))
            throw new UnsupportedOperationException("OpenshiftEnvironmentDriver currently provides support only for the following system image types:" + compatibleImageTypes);

        //TODO: Need to pass the systemImageId and repoUrl to the new environment instead of using system wide environment config
        return new OpenshiftStartedEnvironment(executor, config, pullingMonitor, repositorySession, debugData);
    }

    @Override
    public boolean canRunImageType(SystemImageType systemImageType) {
        if (config.isDisabled()) {
            logger.info("Skipping driver as it is disabled by config.");
            return false;
        }
        return compatibleImageTypes.contains(systemImageType);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }
}
