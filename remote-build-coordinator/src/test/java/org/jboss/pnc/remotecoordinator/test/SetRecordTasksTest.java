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

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.DefaultBuildTaskRef;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SetRecordTasksTest extends AbstractDependentBuildTest {

    private BuildConfiguration configA;
    private BuildConfiguration configB;
    private BuildConfiguration configC;
    private BuildConfiguration configD;
    private BuildConfiguration configE;
    private BuildConfigurationSet configSet;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        super.initialize();

        configA = buildConfig("A");
        configB = buildConfig("B");
        configC = buildConfig("C");
        configD = buildConfig("D");
        configE = buildConfig("E");
        configSet = configSet(configA, configB, configC, configD, configE);

        configSet.getBuildConfigurations().forEach(this::saveConfig);

        // return all configs on queryWithPredicates with configSet ID
        buildConfigurationRepository.returnAllDataOnQuery();
    }

    @Test
    public void buildConfigSetRecordShouldBeMarkedSuccessWhenAllBuildsAreSuccess2() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configB, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configE, BuildStatus.SUCCESS));
        saveBuildConfigSetRecord(configSet, records);

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
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configB, BuildStatus.FAILED));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configE, BuildStatus.SUCCESS));
        saveBuildConfigSetRecord(configSet, records);

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
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.NO_REBUILD_REQUIRED));
        records.add(saveRecordToDB(configB, BuildStatus.NO_REBUILD_REQUIRED));
        records.add(saveRecordToDB(configC, BuildStatus.NO_REBUILD_REQUIRED));
        records.add(saveRecordToDB(configD, BuildStatus.NO_REBUILD_REQUIRED));
        records.add(saveRecordToDB(configE, BuildStatus.NO_REBUILD_REQUIRED));
        saveBuildConfigSetRecord(configSet, records);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.NO_REBUILD_REQUIRED);
    }

    @Test
    public void buildConfigSetRecordShouldBeCancelledIfABuildIsCancelledWithSuccessfulBuilds() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.CANCELLED));
        records.add(saveRecordToDB(configB, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configE, BuildStatus.SUCCESS));
        saveBuildConfigSetRecord(configSet, records);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.CANCELLED);
    }

    @Test
    public void buildConfigSetRecordShouldBeCancelledIfABuildIsCancelledWithFailedBuilds() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.CANCELLED));
        records.add(saveRecordToDB(configB, BuildStatus.FAILED));
        records.add(saveRecordToDB(configC, BuildStatus.FAILED));
        records.add(saveRecordToDB(configD, BuildStatus.FAILED));
        records.add(saveRecordToDB(configE, BuildStatus.FAILED));
        saveBuildConfigSetRecord(configSet, records);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.CANCELLED);
    }

    @Test
    public void buildConfigSetRecordShouldBeCancelledIfABuildIsCancelledWithMixedStatuses() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.CANCELLED));
        records.add(saveRecordToDB(configB, BuildStatus.SYSTEM_ERROR));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.FAILED));
        records.add(saveRecordToDB(configE, BuildStatus.FAILED));
        saveBuildConfigSetRecord(configSet, records);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.CANCELLED);
    }

    @Test
    public void buildConfigSetRecordShouldBeBuildingEvenWithMostCancelled() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();

        // DB
        records.add(saveRecordToDB(configA, BuildStatus.CANCELLED));
        records.add(saveRecordToDB(configB, BuildStatus.SYSTEM_ERROR));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.FAILED));

        var setRecord = saveBuildConfigSetRecord(configSet, records);

        // REX
        saveRecordToRex(configE, BuildStatus.BUILDING, setRecord);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);
    }

    @Test
    public void buildConfigSetRecordShouldBeFailedWithSystemError() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();
        records.add(saveRecordToDB(configA, BuildStatus.SYSTEM_ERROR));
        records.add(saveRecordToDB(configB, BuildStatus.SYSTEM_ERROR));
        records.add(saveRecordToDB(configC, BuildStatus.SUCCESS));
        records.add(saveRecordToDB(configD, BuildStatus.FAILED));
        records.add(saveRecordToDB(configE, BuildStatus.FAILED));
        saveBuildConfigSetRecord(configSet, records);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.FAILED);
    }

    @Test
    public void buildConfigSetRecordShouldBeBuildingWithSuccessfulBuild() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();

        // DB
        records.add(saveRecordToDB(configA, BuildStatus.SUCCESS));
        var setRecord = saveBuildConfigSetRecord(configSet, records);

        // REX
        saveRecordToRex(configB, BuildStatus.BUILDING, setRecord);
        saveRecordToRex(configC, BuildStatus.BUILDING, setRecord);
        saveRecordToRex(configD, BuildStatus.BUILDING, setRecord);
        saveRecordToRex(configE, BuildStatus.BUILDING, setRecord);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);
    }

    @Test
    public void buildConfigSetRecordShouldBeBuildingWithEnqueuedBuilds() throws Exception {
        // with
        var records = new HashSet<BuildRecord>();

        // DB
        records.add(saveRecordToDB(configA, BuildStatus.SUCCESS));
        var setRecord = saveBuildConfigSetRecord(configSet, records);

        // REX
        saveRecordToRex(configB, BuildStatus.ENQUEUED, setRecord);
        saveRecordToRex(configC, BuildStatus.ENQUEUED, setRecord);
        saveRecordToRex(configD, BuildStatus.ENQUEUED, setRecord);
        saveRecordToRex(configE, BuildStatus.ENQUEUED, setRecord);

        var setRecords = buildConfigSetRecordRepository.queryAll();
        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);

        // when
        pokeSetJob();

        // then
        setRecords = buildConfigSetRecordRepository.queryAll();

        assertThat(setRecords).hasSize(1).allMatch(record -> record.getStatus() == BuildStatus.BUILDING);
    }

    private void saveRecordToRex(BuildConfiguration config, BuildStatus status, BuildConfigSetRecord setRecord) {
        taskRepository.addTask(
                DefaultBuildTaskRef.builder()
                        .id(Sequence.nextBase32Id())
                        .idRev(BuildConfigurationAudited.fromBuildConfiguration(config, 1).getIdRev())
                        .status(BuildCoordinationStatus.fromBuildStatus(status))
                        .buildConfigSetRecord(setRecord)
                        .build());
    }

    private BuildRecord saveRecordToDB(BuildConfiguration configA, BuildStatus status) {
        return buildRecordRepository.save(
                BuildRecord.Builder.newBuilder()
                        .buildConfigurationAudited(BuildConfigurationAudited.fromBuildConfiguration(configA, 1))
                        .submitTime(Date.from(Instant.now()))
                        .startTime(Date.from(Instant.now()))
                        .status(status)
                        .endTime(Date.from(Instant.now().plus(1, ChronoUnit.SECONDS)))
                        .build());
    }

    private BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigurationSet configSet, Set<BuildRecord> records) {
        return buildConfigSetRecordRepository.save(
                BuildConfigSetRecord.Builder.newBuilder()
                        .buildRecords(records)
                        .buildConfigurationSet(configSet)
                        .status(BuildStatus.BUILDING)
                        .startTime(Date.from(Instant.now()))
                        .temporaryBuild(false)
                        .build());
    }

}
