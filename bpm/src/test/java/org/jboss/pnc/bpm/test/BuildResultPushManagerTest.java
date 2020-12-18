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

import org.jboss.pnc.bpm.causeway.BuildPushOperation;
import org.jboss.pnc.bpm.causeway.BuildResultPushManager;
import org.jboss.pnc.bpm.causeway.InProgress;
import org.jboss.pnc.bpm.causeway.Result;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.mock.repository.BuildRecordPushResultRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.enterprise.event.Event;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildResultPushManagerTest {

    private static final String BREW_URL_PATTERN = "http://brew.example.com/build/%d/";

    @Mock
    private Event<BuildPushResult> buildRecordPushResultRestEvent;

    @Mock
    private CausewayClient causewayClient;

    private BuildRecordRepositoryMock buildRecordRepository;
    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildRecordPushResultRepositoryMock buildRecordPushResultRepository;

    @Mock
    private ArtifactRepository artifactRepository;

    private BuildResultPushManager releaseManager;

    private int milestoneIdSequence = 0;
    private int buildRecordIdSequence = 0;

    private static final BuildEnvironment be = new BuildEnvironment();
    private static final BuildConfigurationAudited bca = new BuildConfigurationAudited();
    @Mock
    private BuildConfigurationAudited buildConfigurationAudited;

    static {
        bca.setBuildEnvironment(be);
        bca.setBuildType(BuildType.MVN);
    }

    @Before
    public void setUp() throws CoreException {
        when(buildConfigurationAuditedRepository.queryById(any(IdRev.class))).thenReturn(buildConfigurationAudited);
        when(causewayClient.importBuild(any(), any())).thenReturn(true);
        buildRecordRepository = new BuildRecordRepositoryMock();
        buildRecordPushResultRepository = new BuildRecordPushResultRepositoryMock();

        releaseManager = new BuildResultPushManager(
                buildConfigurationAuditedRepository,
                buildRecordPushResultRepository,
                null,
                new InProgress(),
                buildRecordPushResultRestEvent,
                artifactRepository,
                causewayClient);
    }

    @Test
    public void shouldAccept() {
        // given
        BuildRecord record = buildRecord(true);
        record.setExecutionRootName("Foo:bar");
        record.setExecutionRootVersion("baz");

        // when
        Result result = release(record);

        // then
        assertThat(result).extracting(Result::getStatus).isEqualTo(BuildPushStatus.ACCEPTED);
    }

    @Test
    public void shouldRejectWithMissingData() {
        // given
        BuildRecord record = buildRecord(false);

        // when
        Result result = release(record);

        // then
        assertThat(result).extracting(Result::getStatus).isEqualTo(BuildPushStatus.SYSTEM_ERROR);
        assertThat(result.getMessage()).containsIgnoringCase("ExecutionRoot");
    }

    @Test
    public void shouldRejectWhenSameBuildIdIsInProgress() {
        // given
        BuildRecord record = buildRecord(true);

        // when
        release(record);
        Result result = release(record);

        // then
        assertThat(result).extracting(Result::getStatus).isEqualTo(BuildPushStatus.REJECTED);
    }

    private Result release(BuildRecord buildRecord) {
        BuildPushOperation buildPushOperation = new BuildPushOperation(
                buildRecord,
                Sequence.nextId(),
                "tag",
                false,
                "https://foo.bar/build-record-push/%s/complete/");
        return releaseManager.push(buildPushOperation, "abc");
    }

    private BuildRecord buildRecord(boolean withExecutionRootName) {
        BuildRecord record = new BuildRecord();
        record.setId(Sequence.nextId());
        record.setStatus(BuildStatus.SUCCESS);
        record.setBuildConfigurationAudited(bca);
        record.setDependencies(Collections.emptySet());
        if (withExecutionRootName) {
            record.setExecutionRootName("execution:root");
        }
        buildRecordRepository.save(record);
        return record;
    }

}
