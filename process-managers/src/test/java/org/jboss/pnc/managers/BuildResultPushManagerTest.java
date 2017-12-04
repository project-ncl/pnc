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
package org.jboss.pnc.managers;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.managers.causeway.CausewayClient;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.mock.repository.BuildRecordPushResultRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.rest.restmodel.BuildRecordPushResultRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.enterprise.event.Event;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildResultPushManagerTest {

    private static final String EXECUTION_ROOT_NAME = "org.jboss.pnc:parent";
    private static final String EXECUTION_ROOT_VERSION = "1.2.3";
    private static final String SCM_REPO_URL = "SCM_REPO_URL";
    private static final String SCM_REPO_REVISION = "SCM_REPO_REVISION";

    private BuildResultPushManager buildResultPushManager;
    private BuildRecordRepository buildRecordRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    private static final String PUSH_LOG = "Well done push!";

    private Artifact dependency1 = ArtifactBuilder.mockArtifact(1);
    private Artifact dependency2 = ArtifactBuilder.mockArtifact(2);
    private Artifact builtArtifact1 = ArtifactBuilder.mockArtifact(10);
    private Artifact builtArtifact2 = ArtifactBuilder.mockArtifact(11);

    @Before
    public void init() {
        buildRecordRepository = new BuildRecordRepositoryMock();

        buildRecordPushResultRepository = new BuildRecordPushResultRepositoryMock();
        InProgress inProgress = new InProgress();

        Event<BuildRecordPushResultRest> event = Mockito.mock(Event.class);
        CausewayClient causewayClient = new TestCausewayClient();

        buildResultPushManager = new BuildResultPushManager(
                buildRecordRepository,
                buildRecordPushResultRepository,
                inProgress,
                causewayClient,
                event
                );

    }

    private Integer saveBuildRecord(BuildRecordRepository buildRecordRepository) {

        BuildEnvironment environment = BuildEnvironment.Builder.newBuilder()
                .attribute("mvn", "3.0.4")
                .attribute("java", "1.8")
                .build();

        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(environment)
                .build();

        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, 1);

        BuildRecord buildRecord = BuildRecord.Builder.newBuilder()
                .executionRootName(EXECUTION_ROOT_NAME)
                .executionRootVersion(EXECUTION_ROOT_VERSION)
                .startTime(Date.from(Instant.EPOCH))
                .endTime(Date.from(Instant.EPOCH.plus(10, ChronoUnit.SECONDS)))
                .scmRepoURL(SCM_REPO_URL)
                .scmRevision(SCM_REPO_REVISION)
                .dependency(dependency1)
                .dependency(dependency2)
                .builtArtifact(builtArtifact1)
                .builtArtifact(builtArtifact2)
                .buildConfigurationAudited(buildConfigurationAudited)
                .temporaryBuild(false)
                .build();

        return buildRecordRepository.save(buildRecord).getId();
    }

    @Test
    public void push() throws Exception {
        //given
        Integer buildRecordId = saveBuildRecord(buildRecordRepository);

        Set<Integer> buildRecordIds = new HashSet<>();
        buildRecordIds.add(buildRecordId);

        //when
        String tagPrefix = "tagPrefix";
        Map<Integer, Boolean> pushed = buildResultPushManager.push(buildRecordIds, "", "don't/call/me/back", tagPrefix);

        //then expect push started
        Assertions.assertThat(pushed.get(buildRecordId)).isTrue();


        //when pushed again
        Map<Integer, Boolean> pushed2 = buildResultPushManager.push(buildRecordIds, "", "don't/call/me/back", tagPrefix);

        //then expect push is rejected
        Assertions.assertThat(pushed2.get(buildRecordId)).isFalse();

        //when complete
        Integer pushResultId = buildResultPushManager.complete(
                buildRecordId,
                getBuildRecordPushResultRest(buildRecordId).toDBEntityBuilder().build()
        );

        //then the result should be stored
        BuildRecordPushResult buildRecordPushResult = buildRecordPushResultRepository.queryById(pushResultId);
        Assertions.assertThat(buildRecordPushResult.getLog()).isEqualTo(PUSH_LOG);
        Assertions.assertThat(buildRecordPushResult.getStatus()).isEqualTo(BuildRecordPushResult.Status.SUCCESS);

        //then expect push is accepted again
        Map<Integer, Boolean> pushed3 = buildResultPushManager.push(buildRecordIds, "", "don't/call/me/back", tagPrefix);
        Assertions.assertThat(pushed3.get(buildRecordId)).isTrue();

    }

    private BuildRecordPushResultRest getBuildRecordPushResultRest(Integer buildRecordId) {
        return BuildRecordPushResultRest.builder()
                    .status(BuildRecordPushResult.Status.SUCCESS)
                    .log(PUSH_LOG)
                    .buildRecordId(buildRecordId)
                    .build();
    }

    private class TestCausewayClient implements CausewayClient {

        @Override
        public boolean push(String jsonMessage, String authToken) {
            if (jsonMessage.contains(EXECUTION_ROOT_NAME)
                    && jsonMessage.contains(EXECUTION_ROOT_VERSION)
                    && jsonMessage.contains(SCM_REPO_URL)
                    && jsonMessage.contains(SCM_REPO_REVISION)
                    && jsonMessage.contains(dependency1.getFilename())
                    && jsonMessage.contains(dependency1.getMd5())
                    && jsonMessage.contains(dependency2.getFilename())
                    && jsonMessage.contains(dependency2.getMd5())
                    && jsonMessage.contains(builtArtifact1.getFilename())
                    && jsonMessage.contains(builtArtifact1.getMd5())
                    && jsonMessage.contains(builtArtifact2.getFilename())
                    && jsonMessage.contains(builtArtifact2.getMd5())
                    ) {
                return true;
            } else {
                //throw assertion error
                throw new AssertionError("Json message does not contain expected elements. Message: " + jsonMessage);
            }
        }
    }
}