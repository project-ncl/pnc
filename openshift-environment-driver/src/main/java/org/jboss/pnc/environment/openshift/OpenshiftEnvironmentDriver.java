/*
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
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class OpenshiftEnvironmentDriver implements EnvironmentDriver {

    private ExecutorService executor = Executors.newFixedThreadPool(4); //TODO configurable

    private OpenshiftEnvironmentDriverModuleConfig config;
    private PullingMonitor pullingMonitor;

    @Deprecated //CDI workaround
    public OpenshiftEnvironmentDriver() {
    }

    @Inject
    public OpenshiftEnvironmentDriver(Configuration configuration, PullingMonitor pullingMonitor) throws ConfigurationParseException {
        this.pullingMonitor = pullingMonitor;
        config = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftEnvironmentDriverModuleConfig.class));
    }

    @Override
    public StartedEnvironment buildEnvironment(Environment buildEnvironment, RepositorySession repositorySession) throws EnvironmentDriverException {
        if (!canBuildEnvironment(buildEnvironment))
            throw new UnsupportedOperationException("OpenshiftEnvironmentDriver currently provides support only for Linux and JAVA builds.");

        return new OpenshiftStartedEnvironment(executor, config, pullingMonitor, repositorySession);
    }

    @Override
    public boolean canBuildEnvironment(Environment environment) {
        if (config.isDisabled()) {
            return false;
        }
        return environment.getBuildType() == BuildType.JAVA &&
                environment.getOperationalSystem() == OperationalSystem.LINUX;
    }

    @PreDestroy
    public void destroy() {
        executor.shutdownNow();
    }
}
