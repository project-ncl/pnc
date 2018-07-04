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
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.StringUtils;
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

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class OpenshiftEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftEnvironmentDriver.class);
    private static final int DEFAULT_EXECUTOR_THREAD_POOL_SIZE = 4;

    public static List<SystemImageType> compatibleImageTypes = Arrays.asList(SystemImageType.DOCKER_IMAGE);

    //TODO configurable:
    private ExecutorService executor;

    private OpenshiftEnvironmentDriverModuleConfig openshiftEnvironmentDriverModuleConfig;
    private OpenshiftBuildAgentConfig openshiftBuildAgentConfig;
    private PullingMonitor pullingMonitor;

    @Deprecated //CDI workaround
    public OpenshiftEnvironmentDriver() {
    }

    @Inject
    public OpenshiftEnvironmentDriver(Configuration configuration, PullingMonitor pullingMonitor) throws ConfigurationParseException {

        int executorThreadPoolSize = DEFAULT_EXECUTOR_THREAD_POOL_SIZE;
        this.pullingMonitor = pullingMonitor;

        openshiftEnvironmentDriverModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftEnvironmentDriverModuleConfig.class));
        try {
            this.openshiftBuildAgentConfig = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftBuildAgentConfig.class));;
        } catch (ConfigurationParseException e) {
            logger.warn("OpenshiftBuildAgentConfig is not provided or is broken. Using the default built-in config.");
        }

        String executorThreadPoolSizeStr = openshiftEnvironmentDriverModuleConfig.getExecutorThreadPoolSize();

        if (executorThreadPoolSizeStr != null) {
            executorThreadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
        }
        executor = MDCExecutors.newFixedThreadPool(executorThreadPoolSize, new NamedThreadFactory("openshift-environment-driver"));

        logger.info("Is OpenShift environment driver disabled: {}", openshiftEnvironmentDriverModuleConfig.isDisabled());
    }

    @Override
    public StartedEnvironment startEnvironment(
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            RepositorySession repositorySession,
            DebugData debugData,
            String accessToken) throws EnvironmentDriverException {

        if (!canRunImageType(systemImageType))
            throw new UnsupportedOperationException("OpenshiftEnvironmentDriver currently provides support only for the following system image types:" + compatibleImageTypes);
        String buildImageId = StringUtils.addEndingSlash(systemImageRepositoryUrl) + StringUtils.stripTrailingSlash(systemImageId);
        return new OpenshiftStartedEnvironment(
                executor,
                openshiftBuildAgentConfig,
                openshiftEnvironmentDriverModuleConfig,
                pullingMonitor,
                repositorySession,
                buildImageId,
                debugData,
                accessToken);
    }

    @Override
    public boolean canRunImageType(SystemImageType systemImageType) {
        if (openshiftEnvironmentDriverModuleConfig.isDisabled()) {
            logger.info("Skipping driver as it is disabled by openshiftEnvironmentDriverModuleConfig.");
            return false;
        }
        return compatibleImageTypes.contains(systemImageType);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }
}
