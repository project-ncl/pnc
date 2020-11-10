/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.bpm.test;

import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.bpm.model.causeway.BuildImportResultRest;
import org.jboss.pnc.bpm.model.causeway.BuildImportStatus;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.bpm.task.MilestoneReleaseTask;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.enums.ReleaseStatus;
import org.jboss.pnc.mapper.api.ProductMilestoneCloseResultMapper;
import org.jboss.pnc.mock.repository.*;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 */
public class ProductMilestoneReleaseManagerTest {

    private static final String BREW_URL_PATTERN = "http://brew.example.com/build/%d/";

    @Mock
    private BpmManager bpmManager;

    @Mock
    private Event<ProductMilestoneCloseResult> productMilestoneCloseResultEvent;

    @Mock
    private ProductMilestoneCloseResultMapper mapper;

    private ProductMilestoneRepository milestoneRepository;
    private ProductMilestoneReleaseRepository productMilestoneReleaseRepository;
    private BuildRecordRepositoryMock buildRecordRepository;
    private BuildRecordPushResultRepositoryMock buildRecordPushResultRepository;

    private ProductMilestoneReleaseManager releaseManager;
    private final BpmTaskCapture taskCapture = new BpmTaskCapture();

    private int milestoneIdSequence = 0;
    private int buildRecordIdSequence = 0;

    @Before
    public void setUp() throws CoreException {
        milestoneRepository = new ProductMilestoneRepositoryMock();
        productMilestoneReleaseRepository = new ProductMilestoneReleaseRepositoryMock();
        buildRecordRepository = new BuildRecordRepositoryMock();
        buildRecordPushResultRepository = new BuildRecordPushResultRepositoryMock();

        MockitoAnnotations.initMocks(this);
        when(bpmManager.startTask(any())).then(taskCapture);
        releaseManager = new ProductMilestoneReleaseManager(
                productMilestoneReleaseRepository,
                bpmManager,
                new ProductVersionRepositoryMock(),
                buildRecordRepository,
                milestoneRepository,
                buildRecordPushResultRepository,
                mapper,
                productMilestoneCloseResultEvent,
                null,
                null);
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
        assertPushResultLinkedToRecord(record, brewBuildId, brewUrl(brewBuildId));
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
            assertPushResultLinkedToRecord(record, brewBuildId + i, brewUrl(brewBuildId + i));
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

        buildResults
                .add(buildImportResultRest(BuildImportStatus.SUCCESSFUL, buildRecord1.getId(), buildRecord1.getId()));
        buildResults.add(buildImportResultRest(BuildImportStatus.FAILED, buildRecord2.getId(), buildRecord2.getId()));
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

    private BuildImportResultRest buildImportResultRest(BuildImportStatus status, Integer id, Integer id2) {
        return new BuildImportResultRest(id, id2, brewUrl(id2), status, null);
    }

    /**
     * Start a milestone release and process the callback
     */
    private void release(ProductMilestone milestone, int brewBuildId, BuildRecord... records) {
        releaseManager.startRelease(milestone, null, false, Sequence.nextId());
        List<ProductMilestoneRelease> releases = productMilestoneReleaseRepository.queryAll();
        assertThat(releases).hasSize(1);
        taskCapture.task.notify(BpmEventType.BREW_IMPORT, successfulReleaseResult(brewBuildId, records));
    }

    private void release(ProductMilestone milestone, MilestoneReleaseResultRest releaseResultRest) {
        releaseManager.startRelease(milestone, null, false, Sequence.nextId());
        List<ProductMilestoneRelease> releases = productMilestoneReleaseRepository.queryAll();
        assertThat(releases).hasSize(1);
        taskCapture.task.notify(BpmEventType.BREW_IMPORT, releaseResultRest);
    }

    private void assertPushResultLinkedToRecord(BuildRecord record, Integer expectedBrewId, String expectedBrewLink) {
        BuildRecordPushResult pushResult = buildRecordPushResultRepository.getLatestForBuildRecord(record.getId());
        assertThat(pushResult).isNotNull();
        assertThat(pushResult.getBrewBuildId()).isEqualTo(expectedBrewId);
        assertThat(pushResult.getBrewBuildUrl()).isEqualTo(expectedBrewLink);
    }

    private MilestoneReleaseResultRest successfulReleaseResult(int brewBuildId, BuildRecord... records) {
        MilestoneReleaseResultRest result = new MilestoneReleaseResultRest();
        List<BuildImportResultRest> buildResults = new ArrayList<>();

        for (int i = 0; i < records.length; i++) {
            Integer recordId = records[i].getId();
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
        record.setId(buildRecordIdSequence++);
        buildRecordRepository.save(record);
        return record;
    }

    private static class BpmTaskCapture implements Answer<Boolean> {
        private MilestoneReleaseTask task;

        @Override
        public Boolean answer(InvocationOnMock invocation) {
            task = invocation.getArgument(0);
            return true;
        }

    }
}