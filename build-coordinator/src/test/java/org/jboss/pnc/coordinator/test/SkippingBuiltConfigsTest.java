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
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/22/16
 * Time: 2:51 PM
 */
public class SkippingBuiltConfigsTest extends AbstractDependentBuildTest {
    private static final Logger log = LoggerFactory.getLogger(SkippingBuiltConfigsTest.class);

    private BuildConfiguration configA;

    private BuildConfiguration configB;
    private BuildConfiguration configC;
    private BuildConfiguration configD;
    private BuildConfiguration configE;

    private BuildConfigurationSet configSet;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        configA = buildConfig("A");
        configB = buildConfig("B");
        configC = buildConfig("C");
        configD = buildConfig("D");
        configE = buildConfig("E");

        configSet = configSet(configA, configB, configC, configD, configE);

        buildConfigurationRepository = spy(new BuildConfigurationRepositoryMock());
        when(buildConfigurationRepository.queryWithPredicates(any())).thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        configSet.getBuildConfigurations().forEach(bc -> saveConfig(bc));
    }



    @Test
    public void shouldNotBuildTheSameBuildConfigurationTwice() throws Exception {
        coordinator.start();
        buildRecordRepository.clear();
        //given
        BuildConfiguration testConfiguration = config("shouldNotBuildTheSameBuildConfigurationTwice");
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(false);

        //when
        coordinator.build(testConfiguration, null, buildOptions);
        waitForEmptyBuildQueue();

        coordinator.build(testConfiguration, null, buildOptions);
        waitForEmptyBuildQueue();

        //then
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(1);
    }

    @Test
    public void shouldTriggerTheSameBuildConfigurationWithNewRevision() throws Exception {
        coordinator.start();
        buildRecordRepository.clear();
        //given
        BuildConfiguration testConfiguration = config("shouldRejectBCWithNewRevision");
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(false);

        //when
        coordinator.build(testConfiguration, null, buildOptions);
        BuildConfiguration updatedConfiguration = updateConfiguration(testConfiguration);

        BuildSetTask buildSetTask;
        boolean rejected = false;
        try {
            buildSetTask = coordinator.build(updatedConfiguration, null, buildOptions);
        } catch (BuildConflictException e) {
            rejected = true;
        }

        //then
        Assert.assertFalse("The task was rejected.", rejected);
        waitForEmptyBuildQueue();
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(2);
    }

    @Test
    public void shouldNotTriggerTheSameBuildConfigurationViaDependency() throws Exception {
        coordinator.start();
        buildRecordRepository.clear();
        //given
        BuildConfiguration configurationA = config("configurationA");
        BuildConfiguration configurationB = config("configurationB");
        configurationA.addDependency(configurationB);
        BuildOptions buildOptions = new BuildOptions();

        //when
        coordinator.build(configurationB, null, buildOptions);
        coordinator.build(configurationA, null, buildOptions);

        //then
        new PullingMonitor().monitor(() -> {},
                e -> {Assert.fail("There should be 2 submitted tasks.");},
                () -> coordinator.getSubmittedBuildTasks().size() == 2,
                100, 500, TimeUnit.MILLISECONDS);
//        Assert.assertEquals("There should be 2 submitted tasks.", 2, coordinator.getSubmittedBuildTasks().size());
        waitForEmptyBuildQueue();
        Assert.assertEquals("There should be 2 build records.", 2, buildRecordRepository.queryAll().size());
    }

    @Test
    public void shouldBuildConfigurationAndUnbuiltDependency() throws Exception {
        coordinator.start();
        buildRecordRepository.clear();
        //given
        BuildConfiguration testConfiguration = config("shouldBuildConfigurationAndUnbuiltDependency");
        BuildConfiguration dependency = config("dependency");
        testConfiguration.addDependency(dependency);
        BuildOptions buildOptions = new BuildOptions();

        //when
        coordinator.build(testConfiguration, null, buildOptions);
        waitForEmptyBuildQueue();

        //then
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(2);
    }

    @Test
    public void shouldNotRebuildAlreadyBuiltDependency() throws Exception {
        coordinator.start();
        buildRecordRepository.clear();
        //given
        BuildConfiguration testConfiguration = config("shouldNotRebuildAlreadyBuiltDependency");
        BuildConfiguration dependency = config("dependency");
        testConfiguration.addDependency(dependency);
        BuildOptions buildOptions = new BuildOptions();

        coordinator.build(dependency, null, buildOptions);
        waitForEmptyBuildQueue();
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(1);

        //when
        coordinator.build(testConfiguration, null, buildOptions);
        waitForEmptyBuildQueue();

        //then
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(2);
    }

    @Test
    public void shouldRerunTheSameBuildConfigurationIfRebuildAllIsSpecified() throws Exception {
        buildRecordRepository.clear();
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setForceRebuild(true);

        //when
        coordinator.build(configA, null, buildOptions);
        waitForEmptyBuildQueue();

        coordinator.build(configA, null, buildOptions);
        waitForEmptyBuildQueue();

        //then
        List<BuildRecord> buildRecords = getNonRejectedBuildRecords();
        assertThat(buildRecords.size()).isEqualTo(2);
        logRecords(buildRecords);
    }

    @Test
    public void shouldNotBuildTheSameBuildConfigurationSetTwice() throws Exception {
        //when
        BuildOptions buildOptions1 = new BuildOptions();
        coordinator.build(configSet, null, buildOptions1); //first build
        waitForEmptyBuildQueue();

        BuildOptions buildOptions2 = new BuildOptions();
        coordinator.build(configSet, null, buildOptions2); // rebuild build
        waitForEmptyBuildQueue();

        //then
        List<BuildRecord> buildRecords = getNonRejectedBuildRecords();
        logRecords(buildRecords);
        assertThat(buildRecords.size()).isEqualTo(configSet.getBuildConfigurations().size());
    }

    @Test
    public void shouldRerunTheSameBuildConfigurationSetIfRebuildAllIsSpecified() throws Exception {
        //when
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setForceRebuild(true);
        coordinator.build(configSet, null, buildOptions); //first build
        waitForEmptyBuildQueue();


        coordinator.build(configSet, null, buildOptions); //forced rebuild build
        waitForEmptyBuildQueue();
        //then
        List<BuildRecord> buildRecords = getNonRejectedBuildRecords();
        logRecords(buildRecords);
        assertThat(buildRecords.size()).isEqualTo(2 * configSet.getBuildConfigurations().size());
    }

    private List<BuildRecord> getNonRejectedBuildRecords() {
        return buildRecordRepository.queryAll().stream().filter(r -> r.getStatus() != BuildStatus.REJECTED).collect(Collectors.toList());
    }

    private void logRecords(List<BuildRecord> buildRecords) {
        log.trace("Found build records: {}", buildRecords.stream()
                .map(br -> "Br.id: " + br.getId() + ", " + br.getBuildConfigurationAudited().getId().toString())
                .collect(Collectors.joining("; ")));
    }
}
