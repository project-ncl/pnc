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
import org.jboss.pnc.coordinator.test.event.TestCDIBuildSetStatusChangedReceiver;
import org.jboss.pnc.coordinator.test.event.TestCDIBuildStatusChangedReceiver;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.test.util.Wait;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class ProjectWithDependenciesBuildTest extends ProjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProjectWithDependenciesBuildTest.class);

    private static final int BUILD_SET_ID = 14352;

    Set<BuildSetStatusChangedEvent> eventsReceived = new HashSet<>();

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Inject
    TestCDIBuildSetStatusChangedReceiver testCDIBuildSetStatusChangedReceiver;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
        jar.addClass(TestCDIBuildSetStatusChangedReceiver.class);
        jar.addClass(TestCDIBuildStatusChangedReceiver.class);
        jar.addClass(DatastoreMock.class);

        return jar;
    }

    @Test
    public void buildProjectTestCase() throws Exception {
        clearSemaphores();
        // given
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener(this::collectEvent);
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);

        // when
        BuildCoordinatorBeans coordinator = buildCoordinatorFactory.createBuildCoordinator(datastoreMock);
        buildProjects(configurationBuilder.buildConfigurationSet(BUILD_SET_ID), coordinator.coordinator);

        // expect
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        log.trace(
                "Found build records: {}",
                buildRecords.stream()
                        .map(br -> "Br.id: " + br.getId() + ", " + br.getBuildConfigurationAudited().getId().toString())
                        .collect(Collectors.joining("; ")));
        Assert.assertEquals("Wrong datastore results count.", 5, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        BuildStatus status = buildRecord.getStatus();
        Assert.assertEquals("Invalid build status: " + status, BuildStatus.SUCCESS, status);

        assertArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertArtifactsPresent(buildRecord.getDependencies());

        BuildConfigSetRecord buildConfigSetRecord = datastoreMock.getBuildConfigSetRecords().get(0);
        Assert.assertNotNull("End time not set! Record: " + buildConfigSetRecord, buildConfigSetRecord.getEndTime());
        Assert.assertTrue(buildConfigSetRecord.getEndTime().getTime() > buildConfigSetRecord.getStartTime().getTime());
        Assert.assertEquals(BuildStatus.SUCCESS, buildConfigSetRecord.getStatus());

        String events = eventsReceived.stream().map(Object::toString).collect(Collectors.joining("; "));
        Assert.assertEquals("Invalid number of received events. Received events: " + events, 2, eventsReceived.size());
        Wait.forCondition(
                coordinator.queue::isEmpty,
                1,
                ChronoUnit.SECONDS,
                "Not empty build queue: " + coordinator.queue);
    }

    private void collectEvent(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        log.info("Got: build set status changed event for build id: " + BUILD_SET_ID);
        if (buildSetStatusChangedEvent.getBuildSetConfigurationId().equals(Integer.toString(BUILD_SET_ID))) {
            log.info("correct id, saving {}", buildSetStatusChangedEvent);
            eventsReceived.add(buildSetStatusChangedEvent);
        }
    }

}
