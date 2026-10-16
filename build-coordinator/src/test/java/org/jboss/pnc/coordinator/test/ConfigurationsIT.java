/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.InMemory;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsIT extends ProjectBuilder {

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
