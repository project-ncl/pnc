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
package org.jboss.pnc.remotecoordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.remotecoordinator.builder.BuildTasksInitializer;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class ReadDependenciesTest extends ProjectBuilder {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Inject
    private DatastoreAdapter datastoreAdapter;

    @Inject
    private BuildTaskRepository taskRepository;

    @Inject
    Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Test
    public void createDependenciesTestCase() throws DatastoreException {
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet(1);
        User user = User.Builder.newBuilder().id(1).username("test-user").build();
        BuildSetTask buildSetTask = null;
        try {
            buildSetTask = createBuildSetTask(buildConfigurationSet, user);
        } catch (CoreException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals("Missing build tasks in set.", 5, buildSetTask.getBuildTasks().size());
        BuildTask buildTask2 = buildSetTask.getBuildTasks()
                .stream()
                .filter(task -> task.getBuildConfigurationAudited().getId().equals(2))
                .findFirst()
                .get();
        Assert.assertEquals("Wrong number of dependencies.", 2, buildTask2.getDependencies().size());
    }

    public BuildSetTask createBuildSetTask(BuildConfigurationSet buildConfigurationSet, User user)
            throws CoreException {
        BuildTasksInitializer buildTasksInitializer = new BuildTasksInitializer(datastoreAdapter, 1L);
        AtomicInteger atomicInteger = new AtomicInteger(1);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);

        return buildTasksInitializer.createBuildGraph(
                buildConfigurationSet,
                user,
                buildOptions,
                () -> Sequence.nextBase32Id(),
                taskRepository.getUnfinishedTasks());
    }
}
