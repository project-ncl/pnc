/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.mock.repository.BuildRecordPushResultRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.event.Event;

import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.pnc.bpm.causeway.BuildResultPushManager;
import org.jboss.pnc.bpm.causeway.InProgress;
import org.jboss.pnc.bpm.causeway.Result;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

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
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildRecordPushResultRepositoryMock buildRecordPushResultRepository;

    @Mock
    private ArtifactRepository artifactRepository;

    private BuildResultPushManager releaseManager;

    private int milestoneIdSequence = 0;
    private int buildRecordIdSequence = 0;

    private static final BuildEnvironment be = new BuildEnvironment();
    private static final BuildConfigurationAudited bca = new BuildConfigurationAudited();
    @Mock
    private BuildConfiguration bc;

    static {
        bca.setBuildEnvironment(be);
        bca.setBuildType(BuildType.MVN);
    }

    @Before
    public void setUp() throws CoreException {
        when(buildConfigurationRepository.queryById(any())).thenReturn(bc);
        when(causewayClient.importBuild(any(), any())).thenReturn(true);
        buildRecordRepository = new BuildRecordRepositoryMock();
        buildRecordPushResultRepository = new BuildRecordPushResultRepositoryMock();

        releaseManager = new BuildResultPushManager(
                buildConfigurationRepository,
                buildRecordRepository,
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
        BuildRecord record = buildRecord();
        int brewBuildId = 100;
        record.setExecutionRootName("Foo:bar");
        record.setExecutionRootVersion("baz");

        // when
        Set<Result> results = release(brewBuildId, record);

        // then
        assertThat(results).isNotEmpty()
                .first()
                .extracting(Result::getStatus).isEqualTo(BuildPushStatus.ACCEPTED);
    }

    @Test
    public void shouldRejectWithMissingData() {
        // given
        BuildRecord record = buildRecord();
        int brewBuildId = 100;

        // when
        Set<Result> results = release(brewBuildId, record);

        // then
        assertThat(results).isNotEmpty()
                .first()
                .extracting(Result::getStatus).isEqualTo(BuildPushStatus.REJECTED);
        Result result = results.iterator().next();
        assertThat(result.getMessage()).containsIgnoringCase("ExecutionRoot");
    }

    @Test
    public void shouldNotRejectWithPending() {
        // given
        BuildRecord record = buildRecord();
        int brewBuildId = 100;

        // when
        release(brewBuildId, record);
        Set<Result> results = release(brewBuildId, record);

        // then
        assertThat(results).isNotEmpty()
                .first()
                .extracting(Result::getStatus).isEqualTo(BuildPushStatus.REJECTED);
        Result result = results.iterator().next();
        assertThat(result.getMessage()).doesNotContain("already");
    }


    private Set<Result> release(int brewBuildId, BuildRecord... records) {
        Set<String> ids = Arrays.stream(records)
                .map(BuildRecord::getId)
                .map(Object::toString)
                .collect(Collectors.toSet());
        return releaseManager.push(ids, "abc", "https://foo.bar/build-record-push/%s/complete/", "tag", false);
    }

    private BuildRecord buildRecord() {
        BuildRecord record = new BuildRecord();
        record.setId(buildRecordIdSequence++);
        record.setStatus(BuildStatus.SUCCESS);
        record.setBuildConfigurationAudited(bca);
        record.setBuiltArtifacts(Collections.emptySet());
        record.setDependencies(Collections.emptySet());
        buildRecordRepository.save(record);
        return record;
    }

}
