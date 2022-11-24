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

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SetRecordUpdateJobTest extends AbstractDependentBuildTest {

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
        when(buildConfigurationRepository.queryWithPredicates(any()))
                .thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        configSet.getBuildConfigurations().forEach(this::saveConfig);
    }

    @Test
    public void buildConfigSetRecordShouldBeMarkedSuccessWhenAllBuildsAreSuccess() throws Exception {
        // with
        build(configSet, RebuildMode.FORCE);
        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.SUCCESS);
    }

    @Test
    public void buildConfigSetRecordShouldBeMarkedFailedOnFailure() throws Exception {
        // with
        configA.setBuildScript(BuildStatus.FAILED.toString());
        updateConfiguration(configA);

        build(configSet, RebuildMode.FORCE);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.FAILED);
    }

    @Test
    public void buildConfigSetRecordShouldBeNoRebuildRequiredIfAllRecordsAreNRR() throws Exception {
        // with
        build(configSet, RebuildMode.FORCE);
        pokeSetJob();

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1);
        var succRecord = setRecords.iterator().next();
        assertThat(succRecord.getStatus()).isEqualTo(BuildStatus.SUCCESS);

        // when
        build(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(2)
                .filteredOn(record -> !succRecord.getId().equals(record.getId()))
                .allMatch(record -> record.getStatus() == BuildStatus.NO_REBUILD_REQUIRED);
    }

    @Test
    public void buildConfigSetRecordShouldBeMarkedCancelledWhenBuildsAreCancelled() throws Exception {
        // with
        configSet.getBuildConfigurations().forEach(config -> {
            config.setBuildScript(BuildStatus.CANCELLED.toString());
            updateConfiguration(config);
        });

        // when
        build(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.CANCELLED);
    }

}
