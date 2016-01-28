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
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.model.mock.MockUser;
import org.jboss.pnc.test.util.Wait;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class SkippingDependentBuildsTest extends ProjectBuilder {

    Logger log = LoggerFactory.getLogger(SkippingDependentBuildsTest.class);

    @Inject
    private DatastoreMock datastoreMock;

    @Inject
    BuildCoordinator buildCoordinator;

    private BuildConfiguration testConfiguration;
    private User testUser;
    private BuildConfigurationSet testConfigurationSet;

    @Before
    public void before() {
        log.info("Resetting fields to initial state.");
        datastoreMock.clear();
        testConfiguration = configurationBuilder.build(1, "test");
        testUser = MockUser.newTestUser(1);
        testConfigurationSet = configurationBuilder.buildConfigurationSet(1);
    }

    @Test
    @InSequence (10) //prevent parallel execution because we relay on datastore state
    public void shouldNotBuildTheSameBuildConfigurationTwice() throws Exception {
        //when
        buildCoordinator.build(testConfiguration, testUser, false);
        waitForBuild();

        buildCoordinator.build(testConfiguration, testUser, false);
        waitForBuild();

        //then
        assertThat(datastoreMock.getBuildRecords().size()).isEqualTo(1);
    }

    @Test
    @InSequence (20)
    public void shouldRerunTheSameBuildConfigurationIfRebuildAllIsSpecified() throws Exception {
        //when
        buildCoordinator.build(testConfiguration, testUser, true);
        waitForBuild();

        buildCoordinator.build(testConfiguration, testUser, true);
        waitForBuild();

        //then
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        logRecords(buildRecords);
        assertThat(buildRecords.size()).isEqualTo(2);
    }

    @Test
    @InSequence (30)
    public void shouldNotBuildTheSameBuildConfigurationSetTwice() throws Exception {
        //when
        buildCoordinator.build(testConfigurationSet, testUser, false);
        waitForBuild();

        buildCoordinator.build(testConfigurationSet, testUser, false);
        waitForBuild();

        //then
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        logRecords(buildRecords);
        assertThat(buildRecords.size()).isEqualTo(5);
    }

    @Test
    @InSequence (40)
    public void shouldRerunTheSameBuildConfigurationSetIfRebuildAllIsSpecified() throws Exception {
        //when
        buildCoordinator.build(testConfigurationSet, testUser, true);
        waitForBuild();

        buildCoordinator.build(testConfigurationSet, testUser, true);
        waitForBuild();

        //then
        List<BuildRecord> buildRecords = datastoreMock.getBuildRecords();
        logRecords(buildRecords);
        assertThat(buildRecords.size()).isEqualTo(10);
    }

    protected void waitForBuild() throws InterruptedException, TimeoutException {
        Wait.forCondition(() -> !buildCoordinator.hasActiveTasks(), 30, ChronoUnit.SECONDS);
    }

    private void logRecords(List<BuildRecord> buildRecords) {
        log.trace("Found build records: {}", buildRecords.stream()
                .map(br -> "Br.id: " + br.getId() + ", " + br.getBuildConfigurationAudited().getId().toString())
                .collect(Collectors.joining("; ")));
    }
}
