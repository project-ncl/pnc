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
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class SingleProjectBuildTest extends ProjectBuilder {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Test
    public void buildSingleProjectTestCase() throws Exception {
        // given
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        // when
        BuildCoordinator coordinator = buildCoordinatorFactory.createBuildCoordinator(datastoreMock).coordinator;
        buildProject(configurationBuilder.build(1, "c1-java"), coordinator, receivedStatuses::add);

        // expect
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 1, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        BuildStatus status = buildRecord.getStatus();
        Assert.assertEquals("Invalid build status: " + status, BuildStatus.SUCCESS, status);

        assertArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertArtifactsPresent(buildRecord.getDependencies());

        Assert.assertNotNull(buildRecord.getSubmitTime());
        Assert.assertNotNull(buildRecord.getStartTime());
        Assert.assertNotNull(buildRecord.getEndTime());

        receivedStatuses.stream()
                .filter(e -> e.getNewStatus().isFinal())
                .forEach(
                        e -> Assert.assertNotNull(
                                "Final event " + e + " should have end build time.",
                                e.getBuild().getEndTime()));
    }

    @Test
    public void buildWithBasicOptionsTest() throws Exception {
        // given
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        // when
        BuildCoordinator coordinator = buildCoordinatorFactory.createBuildCoordinator(datastoreMock).coordinator;
        BuildTask buildTask = buildProject(
                configurationBuilder.build(1, "c1-java"),
                coordinator,
                receivedStatuses::add);

        // then
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 1, buildRecords.size());
        Assert.assertEquals(new BuildOptions(), buildTask.getBuildOptions());
    }

    @Test
    public void buildWithAdvancedOptionsTest() throws Exception {
        // given
        BuildOptions originalBuildOptions = new BuildOptions(
                true,
                true,
                true,
                true,
                RebuildMode.FORCE,
                AlignmentPreference.PREFER_PERSISTENT);
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);
        List<BuildStatusChangedEvent> receivedStatuses = new CopyOnWriteArrayList<>();

        // when
        BuildCoordinator coordinator = buildCoordinatorFactory.createBuildCoordinator(datastoreMock).coordinator;
        BuildTask buildTask = buildProject(
                configurationBuilder.build(1, "c1-java"),
                coordinator,
                receivedStatuses::add,
                originalBuildOptions);

        // then
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 1, buildRecords.size());
        Assert.assertEquals(originalBuildOptions, buildTask.getBuildOptions());
    }
}
