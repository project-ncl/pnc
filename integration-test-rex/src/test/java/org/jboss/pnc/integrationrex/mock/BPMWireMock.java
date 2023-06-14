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
package org.jboss.pnc.integrationrex.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.bpm.model.RepositoryManagerResultRest;
import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.bpm.model.mapper.RepositoryManagerResultMapper;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.mock.spi.BuildExecutionConfigurationMock;
import org.jboss.pnc.mock.spi.EnvironmentDriverResultMock;
import org.jboss.pnc.mock.spi.RepourResultMock;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.wiremock.webhooks.Webhooks;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.wiremock.webhooks.Webhooks.webhook;

@Slf4j
public class BPMWireMock extends WireMockRule {

    private static final ObjectMapper objectMapper = new JacksonProvider().getMapper();

    public BPMWireMock(int port) {
        super(
                WireMockConfiguration.options()
                        .networkTrafficListener(new ConsoleNotifyingWiremockNetworkTrafficListener())
                        .port(port)
                        .extensions(LogJsonAction.class)
                        .extensions(ResponseTemplateTransformer.builder().global(false).maxCacheEntries(0L).build())
                        .extensions(Webhooks.class));
        stubFor(
                any(urlMatching(".*"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json"))
                        .withPostServeAction(
                                "webhook",
                                webhook().withMethod(RequestMethod.POST)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Authorization", "{{originalRequest.headers.Authorization}}")
                                        .withUrl("{{jsonPath originalRequest.body '$.callback'}}")
                                        .withBody("{ \"status\":true, \"response\": " + mockBuildResult() + "}")
                                        .withFixedDelay(1000)));
    }

    @SneakyThrows
    private static String mockBuildResult() {
        BuildResultMapper mapper = new BuildResultMapper(new RepositoryManagerResultMapper(new ArtifactMapperMock()));

        BuildResult result = new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                "",
                Optional.of(BuildExecutionConfigurationMock.mockConfig()),
                Optional.of(BuildDriverResultMock.mockResult(BuildStatus.SUCCESS)),
                Optional.empty(),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(RepourResultMock.mock()));

        BuildResultRest dto = mapper.toDTO(result);

        // the long jsonPath results in build-id, this ensures Artifacts are unique
        String buildID = "{{jsonPath originalRequest.body '$.payload.initData.task.processParameters.buildExecutionConfiguration.id'}}";

        dto.setRepositoryManagerResult(mockRepositoryManagerResultRest(buildID));

        String s = objectMapper.writeValueAsString(dto);
        log.trace("MOCK-BPM JSON reply: " + s);
        return s;
    }

    public static RepositoryManagerResultRest mockRepositoryManagerResultRest(String buildID) {
        List<Artifact> builtArtifacts = List.of(mockArtifact(buildID + "-1"), mockArtifact(buildID + "-2"));
        List<Artifact> dependencies = List.of(
                mockImportedArtifact(buildID + "-11"),
                mockImportedArtifact(buildID + "-12"),
                mockArtifact(buildID + "-3"));
        String buildContentId = "build-" + buildID;
        String log = "Success";
        CompletionStatus completionStatus = CompletionStatus.SUCCESS;

        return new RepositoryManagerResultRest(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }

    public static final String IDENTIFIER_PREFIX = "org.jboss.pnc:mock.artifact";

    private static Artifact.Builder getArtifactBuilder(String id) {
        return Artifact.builder()
                .identifier(IDENTIFIER_PREFIX + ":" + id)
                .md5("md-fake-ABCDABCD" + id)
                .sha1("sha1-fake-ABCDABCD" + id)
                .sha256("sha256-fake-ABCDABCD" + id)
                .size(12342L)
                .deployPath("http://myrepo.com/org/jboss/mock/artifactFile" + id + ".jar")
                .targetRepository(mockTargetRepository("builds-untested-" + id))
                .filename("artifactFile" + id + ".jar");
    }

    /**
     * Create a generic mock artifact with no associated build record or import URL
     */
    public static Artifact mockArtifact(String id) {
        return getArtifactBuilder(id).build();
    }

    /**
     * Create an artifact with an import date and origin url
     */
    public static Artifact mockImportedArtifact(String id) {
        return getArtifactBuilder(id).importDate(Date.from(Instant.now()).toInstant())
                .originUrl("http://central.maven.org/org/jboss/mock/artifactFile" + id + ".jar")
                .build();
    }

    public static org.jboss.pnc.dto.TargetRepository mockTargetRepository(String path) {
        return org.jboss.pnc.dto.TargetRepository.refBuilder()
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .repositoryPath(path)
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();
    }

    // Simple Artifact Mapper
    private static class ArtifactMapperMock implements ArtifactMapper {

        @Override
        public Artifact toDTO(org.jboss.pnc.model.Artifact dbEntity) {
            if (dbEntity == null) {
                return null;
            }

            Artifact.Builder artifact = Artifact.builder();

            if (dbEntity.getTargetRepository() != null) {
                TargetRepository tr = dbEntity.getTargetRepository();
                artifact.targetRepository(
                        org.jboss.pnc.dto.TargetRepository.refBuilder()
                                .id(String.valueOf(tr.getId()))
                                .repositoryPath(tr.getRepositoryPath())
                                .repositoryType(tr.getRepositoryType())
                                .identifier(tr.getIdentifier())
                                .temporaryRepo(tr.getTemporaryRepo())
                                .build());
            }
            if (dbEntity.getId() != null) {
                artifact.id(String.valueOf(dbEntity.getId()));
            }
            artifact.identifier(dbEntity.getIdentifier());
            artifact.purl(dbEntity.getPurl());
            artifact.artifactQuality(dbEntity.getArtifactQuality());
            artifact.buildCategory(dbEntity.getBuildCategory());
            artifact.md5(dbEntity.getMd5());
            artifact.sha1(dbEntity.getSha1());
            artifact.sha256(dbEntity.getSha256());
            artifact.filename(dbEntity.getFilename());
            artifact.deployPath(dbEntity.getDeployPath());
            if (dbEntity.getImportDate() != null) {
                artifact.importDate(dbEntity.getImportDate().toInstant());
            }
            artifact.originUrl(dbEntity.getOriginUrl());
            artifact.size(dbEntity.getSize());
            if (dbEntity.getCreationTime() != null) {
                artifact.creationTime(dbEntity.getCreationTime().toInstant());
            }
            if (dbEntity.getModificationTime() != null) {
                artifact.modificationTime(dbEntity.getModificationTime().toInstant());
            }
            artifact.qualityLevelReason(dbEntity.getQualityLevelReason());

            return artifact.build();
        }

        @Override
        public ArtifactRef toRef(org.jboss.pnc.model.Artifact dbEntity) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }

        @Override
        public org.jboss.pnc.model.Artifact toEntity(Artifact dtoEntity) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }

        @Override
        public org.jboss.pnc.model.Artifact toEntityWithTransientTargetRepository(Artifact dtoEntity) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }

        @Override
        public void updateEntity(Artifact dtoEntity, org.jboss.pnc.model.Artifact target) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED");
        }
    }
}
