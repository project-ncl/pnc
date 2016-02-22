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

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.core.test.buildCoordinator.event.TestCDIBuildSetStatusChangedReceiver;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class ProjectWithDependenciesBuildTest extends ProjectBuilder {

    Logger log = LoggerFactory.getLogger(ProjectWithDependenciesBuildTest.class);

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Inject
    TestCDIBuildSetStatusChangedReceiver testCDIBuildSetStatusChangedReceiver;

    private static final int BUILD_SET_ID = 14352;
    Set<BuildSetStatusChangedEvent> eventsReceived = new HashSet<>();

    private void collectEvent(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (buildSetStatusChangedEvent.getBuildSetConfigurationId() == BUILD_SET_ID) {
            eventsReceived.add(buildSetStatusChangedEvent);
        }
    }

    @Test
    public void buildProjectTestCase() throws Exception {
        //given
        testCDIBuildSetStatusChangedReceiver.addBuildSetStatusChangedEventListener((e) -> collectEvent(e));
        DatastoreMock datastoreMock = new DatastoreMock();
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastoreMock);

        //when
        buildProjects(configurationBuilder.buildConfigurationSet(14352), buildCoordinatorFactory.createBuildCoordinator(datastoreMock));

        //expect
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        log.trace("Found build records: {}", buildRecords.stream()
                .map(br -> "Br.id: " + br.getId() + ", " + br.getBuildConfigurationAudited().getId().toString())
                .collect(Collectors.joining("; ")));
        Assert.assertEquals("Wrong datastore results count.", 5, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        String buildLog = buildRecord.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));

        assertBuildArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertDependencyArtifactsPresent(buildRecord.getDependencies());

        BuildConfigSetRecord buildConfigSetRecord = datastoreMock.getBuildConfigSetRecords().get(0);
        Assert.assertNotNull(buildConfigSetRecord.getEndTime());
        Assert.assertTrue(buildConfigSetRecord.getEndTime().getTime() > buildConfigSetRecord.getStartTime().getTime());
        Assert.assertEquals(BuildStatus.SUCCESS, buildConfigSetRecord.getStatus());

        String events = eventsReceived.stream().map(e -> e.toString()).collect(Collectors.joining("; "));
        Assert.assertEquals("Invalid number of received events. Received events: " + events, 2, eventsReceived.size());
    }

}
