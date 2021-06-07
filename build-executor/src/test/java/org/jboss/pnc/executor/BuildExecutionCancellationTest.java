/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.executor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.executor.servicefactories.BuildDriverFactory;
import org.jboss.pnc.executor.servicefactories.EnvironmentDriverFactory;
import org.jboss.pnc.executor.servicefactories.RepositoryManagerFactory;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildExecutionCancellationTest extends BuildExecutionBase {

    private static final Logger log = LoggerFactory.getLogger(BuildExecutionCancellationTest.class);

    @Inject
    TestProjectConfigurationBuilder configurationBuilder;

    @Inject
    RepositoryManagerFactory repositoryManagerFactory;

    @Inject
    EnvironmentDriverFactory environmentDriverFactory;

    @Inject
    BuildDriverFactory buildDriverFactory;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildExecutorDeployments.deployment(BuildExecutorDeployments.Options.BLOCKED_BUILD_DRIVER);
    }

    @Test(timeout = 3000)
    public void testBuild() throws ExecutorException, TimeoutException, InterruptedException, BuildDriverException,
            ConfigurationParseException {
        BuildConfiguration buildConfiguration = configurationBuilder.build(1, "c1-java");
        Set<BuildExecutionStatusChangedEvent> statusChangedEvents = new HashSet<>();
        ObjectWrapper<BuildResult> buildExecutionResultWrapper = new ObjectWrapper<>();

        DefaultBuildExecutor executor = new DefaultBuildExecutor(
                repositoryManagerFactory,
                buildDriverFactory,
                environmentDriverFactory,
                new Configuration(),
                null);

        Consumer<BuildExecutionStatusChangedEvent> cancel = (e) -> {
            if (BuildExecutionStatus.BUILD_WAITING.equals(e.getNewStatus())) {
                try {
                    log.info("Cancelling build ...");
                    Thread.sleep(100);
                    executor.cancel(e.getBuildTaskId());
                } catch (ExecutorException | InterruptedException e0) {
                    e0.printStackTrace();
                }
            }
        };

        runBuild(buildConfiguration, statusChangedEvents, buildExecutionResultWrapper, cancel, executor);

        List<BuildExecutionStatus> expectedStatuses = getBuildExecutionStatusesBase();
        expectedStatuses.add(BuildExecutionStatus.CANCELLED);

        // check build statuses
        checkBuildStatuses(statusChangedEvents, expectedStatuses);

        // check results
        BuildResult buildResult = buildExecutionResultWrapper.get();

        BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();

        Assert.assertEquals(BuildStatus.CANCELLED, buildDriverResult.getBuildStatus());

    }

}
