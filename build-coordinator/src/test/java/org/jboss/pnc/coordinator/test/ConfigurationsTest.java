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
package org.jboss.pnc.coordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.InMemory;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsTest extends ProjectBuilder {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Inject
    @InMemory
    BuildCoordinator buildCoordinator;

    @Test(expected = PersistenceException.class) // TODO test is not run as expected exception is thrown
                                                 // configurationBuilder.build...
    @InSequence(10)
    public void dependsOnItselfConfigurationTestCase() throws Exception {

        BuildConfiguration buildConfiguration = configurationBuilder.buildConfigurationWhichDependsOnItself();

        User user = User.Builder.newBuilder().id(1).build();

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(false);

        BuildSetTask taskSet = buildCoordinator.buildConfig(buildConfiguration, user, buildOptions);
        Set<BuildTask> buildTasks = taskSet.getBuildTasks();
        assertThat(buildTasks).hasSize(1);
        BuildTask buildTask = buildTasks.iterator().next();
        Assert.assertEquals(BuildCoordinationStatus.REJECTED, buildTask.getStatus());
        Assert.assertTrue(
                "Invalid status description: " + buildTask.getStatusDescription(),
                buildTask.getStatusDescription().contains("itself"));
    }

    @Test(expected = PersistenceException.class) // TODO test is not run as expected exception is thrown
                                                 // configurationBuilder.build...
    @InSequence(15)
    public void cycleConfigurationTestCase() throws Exception {

        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSetWithCycleDependency();

        User user = User.Builder.newBuilder().id(1).build();

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        BuildSetTask buildSetTask = buildCoordinator.buildSet(buildConfigurationSet, user, buildOptions);
        Assert.assertEquals(BuildSetStatus.REJECTED, buildSetTask.getStatus());
        Assert.assertTrue(
                "Invalid status description: " + buildSetTask.getStatusDescription(),
                buildSetTask.getStatusDescription().contains("Cycle dependencies found"));
    }

}
