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
package org.jboss.pnc.bpm;

import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.bpm.model.causeway.BuildImportResultRest;
import org.jboss.pnc.bpm.model.causeway.BuildImportStatus;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.enums.ReleaseStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneCloseResultMapper;
import org.jboss.pnc.mock.common.BpmModuleConfigMock;
import org.jboss.pnc.mock.common.GlobalModuleGroupMock;
import org.jboss.pnc.mock.repository.BuildRecordPushResultRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.mock.repository.ProductMilestoneReleaseRepositoryMock;
import org.jboss.pnc.mock.repository.ProductMilestoneRepositoryMock;
import org.jboss.pnc.mock.repository.ProductVersionRepositoryMock;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 */
public class ProductMilestoneReleaseManagerTest {

    private static final String BREW_URL_PATTERN = "http://brew.example.com/build/%d/";

    private AtomicInteger sequence = new AtomicInteger();

    @Mock
    private Event<ProductMilestoneCloseResult> productMilestoneCloseResultEvent;

    @Mock
    private ProductMilestoneCloseResultMapper mapper;

    private ProductMilestoneRepository milestoneRepository;
    private ProductMilestoneReleaseRepository productMilestoneReleaseRepository;
    private BuildRecordRepositoryMock buildRecordRepository;
    private BuildRecordPushResultRepositoryMock buildRecordPushResultRepository;

    private ProductMilestoneReleaseManager releaseManager;

    private int milestoneIdSequence = 0;

    @Before
    public void setUp() throws Exception {
        milestoneRepository = new ProductMilestoneRepositoryMock();
        productMilestoneReleaseRepository = new ProductMilestoneReleaseRepositoryMock();
        buildRecordRepository = new BuildRecordRepositoryMock();
        buildRecordPushResultRepository = new BuildRecordPushResultRepositoryMock();

        MockitoAnnotations.initMocks(this);
        Connector connector = new MockConnector();
        releaseManager = new ProductMilestoneReleaseManager(
                productMilestoneReleaseRepository,
                new ProductVersionRepositoryMock(),
                buildRecordRepository,
                milestoneRepository,
                buildRecordPushResultRepository,
                mapper,
                productMilestoneCloseResultEvent,
                GlobalModuleGroupMock.get(),
                BpmModuleConfigMock.get(),
                connector);
    }

    @Test
    public void shouldStoreSuccess() {
        // given
        ProductMilestone milestone = createMilestone();
        BuildRecord record = buildRecord(milestone);
        int brewBuildId = 100;
        // when
        release(milestone, brewBuildId, record);
        // then
        assertPushResultLinkedToRecord(record.getId(), brewBuildId, brewUrl(brewBuildId));
    }

    @Test
    public void shouldStoreSuccessForTwoBuilds() {
        // given
        ProductMilestone milestone = createMilestone();
        BuildRecord[] records = { buildRecord(milestone), buildRecord(milestone) };

        int brewBuildId = 1000;
        // when
        release(milestone, brewBuildId, records);

        // then
        for (int i = 0; i < 2; i++) {
            BuildRecord record = records[i];
            assertPushResultLinkedToRecord(record.getId(), brewBuildId + i, brewUrl(brewBuildId + i));
        }
    }

    @Test
    public void shouldStorePartialImport() {
        // given
        ProductMilestone milestone = createMilestone();
        BuildRecord buildRecord1 = buildRecord(milestone);
        BuildRecord buildRecord2 = buildRecord(milestone);

        MilestoneReleaseResultRest result = new MilestoneReleaseResultRest();
        List<BuildImportResultRest> buildResults = new ArrayList<>();

        buildResults.add(buildImportResultRest(BuildImportStatus.SUCCESSFUL, buildRecord1.getId(), 1));
        buildResults.add(buildImportResultRest(BuildImportStatus.FAILED, buildRecord2.getId(), 2));
        result.setMilestoneId(milestone.getId());
        result.setBuilds(buildResults);
        result.setReleaseStatus(ReleaseStatus.IMPORT_ERROR);

        // when
        release(milestone, result);

        // then
        BuildRecordPushResult pushResult1 = buildRecordPushResultRepository
                .getLatestForBuildRecord(buildRecord1.getId());
        assertThat(pushResult1).isNotNull();
        assertThat(pushResult1.getStatus()).isEqualTo(BuildPushStatus.SUCCESS);

        BuildRecordPushResult pushResult2 = buildRecordPushResultRepository
                .getLatestForBuildRecord(buildRecord2.getId());
        assertThat(pushResult2).isNotNull();
        assertThat(pushResult2.getStatus()).isEqualTo(BuildPushStatus.FAILED);

        List<ProductMilestoneRelease> releases = productMilestoneReleaseRepository.queryAll();
        assertThat(releases).hasSize(1)
                .first()
                .extracting(ProductMilestoneRelease::getStatus)
                .isEqualTo(MilestoneCloseStatus.FAILED);
    }

    private BuildImportResultRest buildImportResultRest(
            BuildImportStatus status,
            Base32LongID buildId,
            Integer brewId) {
        return new BuildImportResultRest(BuildMapper.idMapper.toDto(buildId), brewId, brewUrl(brewId), status, null);
    }

    /**
     * Start a milestone release and process the callback
     */
    private void release(ProductMilestone milestone, int brewBuildId, BuildRecord... records) {
        int milestoneId = sequence.getAndIncrement();
        releaseManager.startRelease(milestone, null, (long) milestoneId);
        List<ProductMilestoneRelease> releases = productMilestoneReleaseRepository.queryAll();
        assertThat(releases).hasSize(1);
        MilestoneReleaseResultRest milestoneReleaseResult = successfulReleaseResult(milestoneId, brewBuildId, records);
        releaseManager.productMilestoneCloseCompleted(milestoneReleaseResult);
    }

    private void release(ProductMilestone milestone, MilestoneReleaseResultRest releaseResultRest) {
        releaseManager.startRelease(milestone, null, Long.valueOf(releaseResultRest.getMilestoneId()));
        List<ProductMilestoneRelease> releases = productMilestoneReleaseRepository.queryAll();
        assertThat(releases).hasSize(1);
        releaseManager.productMilestoneCloseCompleted(releaseResultRest);
    }

    private void assertPushResultLinkedToRecord(
            Base32LongID recordId,
            Integer expectedBrewId,
            String expectedBrewLink) {
        BuildRecordPushResult pushResult = buildRecordPushResultRepository.getLatestForBuildRecord(recordId);
        assertThat(pushResult).isNotNull();
        assertThat(pushResult.getBrewBuildId()).isEqualTo(expectedBrewId);
        assertThat(pushResult.getBrewBuildUrl()).isEqualTo(expectedBrewLink);
    }

    private MilestoneReleaseResultRest successfulReleaseResult(
            int milestoneId,
            int brewBuildId,
            BuildRecord... records) {
        MilestoneReleaseResultRest result = new MilestoneReleaseResultRest();
        result.setMilestoneId(milestoneId);
        List<BuildImportResultRest> buildResults = new ArrayList<>();

        for (int i = 0; i < records.length; i++) {
            Base32LongID recordId = records[i].getId();
            buildResults.add(buildImportResultRest(BuildImportStatus.SUCCESSFUL, recordId, brewBuildId + i));
        }

        result.setBuilds(buildResults);
        result.setReleaseStatus(ReleaseStatus.SUCCESS);
        return result;
    }

    private String brewUrl(int brewBuildId) {
        return String.format(BREW_URL_PATTERN, brewBuildId);
    }

    private ProductMilestone createMilestone() {
        ProductMilestone milestone = new ProductMilestone();
        milestone.setId(milestoneIdSequence++);
        milestone.setProductVersion(new ProductVersion());
        milestoneRepository.save(milestone);

        return milestone;
    }

    private BuildRecord buildRecord(ProductMilestone milestone) {
        BuildRecord record = new BuildRecord();
        record.setProductMilestone(milestone);
        record.setId(new Base32LongID(Sequence.nextId()));
        buildRecordRepository.save(record);
        return record;
    }

    /**
     * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
     */
    public static class MockConnector implements Connector {
        @Override
        public Long startProcess(String processId, Object processParameters, String accessToken)
                throws ProcessManagerException {
            return 1L;
        }

        @Override
        public Long startProcess(String processId, Object requestObject, String correlationKey, String accessToken)
                throws ProcessManagerException {
            return 1L;
        }

        @Override
        public boolean isProcessInstanceCompleted(Long processInstanceId) {
            return false;
        }

        @Override
        public boolean cancelByCorrelation(String correlationKey, String accessToken) {
            return false;
        }

        @Override
        public boolean cancel(Long processInstanceId, String accessToken) {
            return false;
        }

        @Override
        public void close() {

        }
    }
}
