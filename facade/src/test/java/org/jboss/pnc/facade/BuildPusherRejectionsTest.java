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
package org.jboss.pnc.facade;

import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.impl.BrewPusherImpl;
import org.jboss.pnc.facade.validation.OperationNotAllowedException;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jboss.pnc.enums.ArtifactQuality.BLACKLISTED;
import static org.jboss.pnc.enums.ArtifactQuality.DELETED;
import static org.jboss.pnc.enums.ArtifactQuality.NEW;
import static org.jboss.pnc.enums.BuildStatus.FAILED;
import static org.jboss.pnc.enums.BuildStatus.NO_REBUILD_REQUIRED;
import static org.jboss.pnc.enums.BuildStatus.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(Parameterized.class)
public class BuildPusherRejectionsTest {

    @Mock
    GlobalModuleGroup globalModuleGroup;

    @Mock
    private ArtifactRepository artifactRepository;

    @Spy
    private BuildRecordRepository buildRecordRepository = new BuildRecordRepositoryMock();

    @InjectMocks
    BrewPusherImpl brewPusher = new BrewPusherImpl();

    private BuildStatus buildStatus;
    private ArtifactQuality artifactQuality;
    private Class<Exception> expected;

    public BuildPusherRejectionsTest(
            BuildStatus buildStatus,
            ArtifactQuality artifactQuality,
            Class<Exception> expected) {
        this.buildStatus = buildStatus;
        this.artifactQuality = artifactQuality;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static List<Object[]> testQualities() {
        return Arrays.asList(
                new Object[][] { { SUCCESS, BLACKLISTED, OperationNotAllowedException.class },
                        { SUCCESS, DELETED, OperationNotAllowedException.class },
                        { FAILED, NEW, OperationNotAllowedException.class },
                        { NO_REBUILD_REQUIRED, NEW, OperationNotAllowedException.class } });
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldRejectWithBlacklistedArtifacts() throws ProcessException {
        // given
        BuildRecord record = new BuildRecord();
        record.setStatus(buildStatus);
        BuildRecord savedBuildRecord = buildRecordRepository.save(record);

        Artifact artifact = Artifact.builder().build();
        artifact.setArtifactQuality(artifactQuality);
        artifact.setBuildRecord(savedBuildRecord);

        when(globalModuleGroup.getPncUrl()).thenReturn("http://localhost/");
        when(artifactRepository.queryWithPredicates(any())).thenReturn(Collections.singletonList(artifact));
        when(buildRecordRepository.getLatestSuccessfulBuildRecord(any(IdRev.class), any(Boolean.class)))
                .thenReturn(null);

        // then
        thrown.expect(expected);

        // when
        BuildPushParameters buildPushParameters = BuildPushParameters.builder().build();
        brewPusher.pushBuild(savedBuildRecord.getId().toString(), buildPushParameters);

    }
}
