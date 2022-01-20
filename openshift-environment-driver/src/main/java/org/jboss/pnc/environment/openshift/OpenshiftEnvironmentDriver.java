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

package org.jboss.pnc.environment.openshift;

import org.jboss.pnc.common.Date.ExpiresDate;
import org.jboss.pnc.common.concurrent.MDCExecutors;
import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.monitor.PollingMonitor;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.pncmetrics.MetricsConfiguration;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class OpenshiftEnvironmentDriver implements EnvironmentDriver {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftEnvironmentDriver.class);
    private static final int DEFAULT_EXECUTOR_THREAD_POOL_SIZE = 4;

    public static List<SystemImageType> compatibleImageTypes = Arrays.asList(SystemImageType.DOCKER_IMAGE);

    // TODO configurable:
    private ExecutorService executor;

    private OpenshiftEnvironmentDriverModuleConfig openshiftEnvironmentDriverModuleConfig;
    private OpenshiftBuildAgentConfig openshiftBuildAgentConfig;
    private SystemConfig systemConfig;
    private PollingMonitor pollingMonitor;
    private MetricsConfiguration metricsConfig;

    @Deprecated // CDI workaround
    public OpenshiftEnvironmentDriver() {
    }

    @Inject
    public OpenshiftEnvironmentDriver(
            PollingMonitor pollingMonitor,
            SystemConfig systemConfig,
            OpenshiftEnvironmentDriverModuleConfig openshiftEnvironmentDriverModuleConfig,
            OpenshiftBuildAgentConfig openshiftBuildAgentConfig,
            MetricsConfiguration metricsConfig) throws ConfigurationParseException {
        this.systemConfig = systemConfig;

        int executorThreadPoolSize = DEFAULT_EXECUTOR_THREAD_POOL_SIZE;
        this.pollingMonitor = pollingMonitor;

        this.openshiftEnvironmentDriverModuleConfig = openshiftEnvironmentDriverModuleConfig;
        this.openshiftBuildAgentConfig = openshiftBuildAgentConfig;

        String executorThreadPoolSizeStr = openshiftEnvironmentDriverModuleConfig.getExecutorThreadPoolSize();

        if (executorThreadPoolSizeStr != null) {
            executorThreadPoolSize = Integer.parseInt(executorThreadPoolSizeStr);
        }
        executor = MDCExecutors
                .newFixedThreadPool(executorThreadPoolSize, new NamedThreadFactory("openshift-environment-driver"));
        this.metricsConfig = metricsConfig;

        logger.info(
                "Is OpenShift environment driver disabled: {}",
                openshiftEnvironmentDriverModuleConfig.isDisabled());
    }

    @Override
    public StartedEnvironment startEnvironment(
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            RepositorySession repositorySession,
            DebugData debugData,
            String accessToken,
            boolean tempBuild,
            Map<String, String> parameters) throws EnvironmentDriverException {

        if (!canRunImageType(systemImageType))
            throw new UnsupportedOperationException(
                    "OpenshiftEnvironmentDriver currently provides support only for the following system image types:"
                            + compatibleImageTypes);
        String buildImageId = StringUtils.addEndingSlash(systemImageRepositoryUrl)
                + StringUtils.stripTrailingSlash(systemImageId);
        return new OpenshiftStartedEnvironment(
                executor,
                openshiftBuildAgentConfig,
                openshiftEnvironmentDriverModuleConfig,
                pollingMonitor,
                repositorySession,
                buildImageId,
                debugData,
                accessToken,
                tempBuild,
                ExpiresDate.getTemporaryBuildExpireDate(systemConfig.getTemporaryBuildsLifeSpan(), tempBuild),
                metricsConfig,
                parameters);
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
