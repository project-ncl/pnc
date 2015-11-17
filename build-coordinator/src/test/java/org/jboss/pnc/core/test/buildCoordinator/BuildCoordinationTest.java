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

package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.core.test.mock.TestEntitiesFactory;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.jboss.pnc.core.test.buildCoordinator.BuildCoordinatorDeployments.Options.WITH_DATASTORE;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class BuildCoordinationTest {

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    TestProjectConfigurationBuilder testProjectConfigurationBuilder;

    @Inject
    BuildSetStatusNotifications buildSetStatusNotifications;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(WITH_DATASTORE)
                .addClass(TestProjectConfigurationBuilder.class);
    }

    @Test
    public void buildRecordSetShouldBeMarkedSuccessWhenAllBuildsAreSuccess() throws CoreException, TimeoutException, InterruptedException {
        BuildConfigurationSet buildConfigurationSet = TestEntitiesFactory.newBuildConfigurationSet();
        testProjectConfigurationBuilder.buildConfigurationWithDependencies(buildConfigurationSet);
        BuildSetTask buildSetTask = buildCoordinator.build(buildConfigurationSet, TestEntitiesFactory.newUser(), true);

        ObjectWrapper<BuildSetStatus> lastBuildSetStatus = new ObjectWrapper<>();

        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                lastBuildSetStatus.set(statusChangedEvent.getNewStatus());
            }
        };

        BuildSetCallBack buildSetCallBack = new BuildSetCallBack(buildSetTask.getId(), onStatusUpdate);
        buildSetStatusNotifications.subscribe(buildSetCallBack);

        Set<BuildTask> buildTasks = buildSetTask.getBuildTasks();

        Wait.forCondition(() -> lastBuildSetStatus.isSet(), 5, ChronoUnit.SECONDS);

        //check the result
        Assert.assertEquals(BuildSetStatus.DONE, lastBuildSetStatus.get());
        Assert.assertEquals(BuildStatus.SUCCESS, buildSetTask.getBuildConfigSetRecord().getStatus());
    }
}
