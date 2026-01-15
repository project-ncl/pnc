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
import lombok.SneakyThrows;
import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.api.orch.dto.BuildResultRest;
import org.jboss.pnc.api.orch.dto.RepositoryManagerResultRest;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.mapper.ArtifactRepositoryMapperImpl;
import org.jboss.pnc.mapper.BuildDriverResultMapperImpl;
import org.jboss.pnc.mapper.BuildExecutionConfigurationMapperImpl;
import org.jboss.pnc.mapper.BuildResultMapperImpl;
import org.jboss.pnc.mapper.EnvironmentDriverResultMapperImpl;
import org.jboss.pnc.mapper.ProcessExceptionMapper;
import org.jboss.pnc.mapper.RepositoryManagerResultMapperImpl;
import org.jboss.pnc.mapper.RepourResultMapperImpl;
import org.jboss.pnc.mapper.SshCredentialsMapperImpl;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mock.spi.BuildDriverResultMock;
import org.jboss.pnc.mock.spi.BuildExecutionConfigurationMock;
import org.jboss.pnc.mock.spi.EnvironmentDriverResultMock;
import org.jboss.pnc.mock.spi.RepourResultMock;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.BuildResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BPMResultsMock {

    private static final Logger logger = LoggerFactory.getLogger(BPMResultsMock.class);

    private static final ObjectMapper objectMapper = new JacksonProvider().getMapper();

    @SneakyThrows
    public static String mockBuildResultSuccess() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", true);
        result.put("response", mockBuildResult());
        return objectMapper.writeValueAsString(result);
    }

    @SneakyThrows
    public static String mockBuildResultFailed() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", false);
        result.put("response", mockFailedBuildResult());
        return objectMapper.writeValueAsString(result);
    }

    @SneakyThrows
    public static BuildResultRest mockBuildResult() {
        var mapper = getBuildResultMapper();

        BuildResult result = new BuildResult(
                CompletionStatus.SUCCESS,
                Optional.empty(),
                Optional.of(BuildExecutionConfigurationMock.mockConfig()),
                Optional.of(BuildDriverResultMock.mockResult(BuildStatus.SUCCESS)),
                Optional.empty(),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(RepourResultMock.mock()),
                List.of(),
                Map.of());
        BuildResultRest dto = mapper.toDTO(result);

        // the long jsonPath results in build-id, this ensures Artifacts are unique
        String buildID = "{{jsonPath originalRequest.body '$.payload.buildContentId'}}";

        dto.setRepositoryManagerResult(mockRepositoryManagerResultRest(buildID));

        String s = objectMapper.writeValueAsString(dto);
        logger.trace("MOCK-BPM JSON reply: " + s);
        return dto;
    }

    @SneakyThrows
    public static BuildResultRest mockFailedBuildResult() {
        var mapper = getBuildResultMapper();

        BuildResult result = new BuildResult(
                CompletionStatus.FAILED,
                Optional.empty(),
                Optional.of(BuildExecutionConfigurationMock.mockConfig()),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(RepourResultMock.mockFailed()),
                List.of(),
                Map.of());

        BuildResultRest dto = mapper.toDTO(result);

        // the long jsonPath results in build-id, this ensures Artifacts are unique
        String buildID = "{{jsonPath originalRequest.body '$.payload.buildContentId'}}";

        dto.setRepositoryManagerResult(mockRepositoryManagerResultRest(buildID));

        String s = objectMapper.writeValueAsString(dto);
        logger.trace("MOCK-BPM JSON reply: " + s);
        return dto;
    }

    private static BuildResultMapperImpl getBuildResultMapper() {
        var art = new ArtifactRepositoryMapperImpl();
        var becMapper = new BuildExecutionConfigurationMapperImpl(art);
        var envMapper = new EnvironmentDriverResultMapperImpl(new SshCredentialsMapperImpl());
        var bdrMapper = new BuildDriverResultMapperImpl();
        var rrMapper = new RepourResultMapperImpl();
        var artMapper = new ArtifactMapperMock();
        var rmrMapper = new RepositoryManagerResultMapperImpl(artMapper);
        var peMapper = new ProcessExceptionMapper();
        var resultMapper = new BuildResultMapperImpl(
                becMapper,
                envMapper,
                bdrMapper,
                rrMapper,
                rmrMapper,
                peMapper,
                artMapper);
        return resultMapper;
    }

    public static RepositoryManagerResultRest mockRepositoryManagerResultRest(String buildID) {
        List<Artifact> builtArtifacts = List.of(mockArtifact(buildID + "-1"), mockArtifact(buildID + "-2"));
        List<Artifact> dependencies = List.of(
                mockImportedArtifact(buildID + "-11"),
                mockImportedArtifact(buildID + "-12"),
                mockArtifact(buildID + "-3"));
        String buildContentId = "build-" + buildID;
        CompletionStatus completionStatus = CompletionStatus.SUCCESS;

        return new RepositoryManagerResultRest(builtArtifacts, dependencies, buildContentId, completionStatus);
    }

    public static final String IDENTIFIER_PREFIX = "org.jboss.pnc:mock.artifact";

    private static Artifact.Builder getArtifactBuilder(String id) {
        return Artifact.builder()
                .identifier(IDENTIFIER_PREFIX + ":" + id)
                .md5("md-fake-AB" + id)
                .sha1("sha1-fake-AB" + id)
                .sha256("sha256-fake-AB" + id)
                .size(12342L)
                .buildCategory(BuildCategory.STANDARD)
                .artifactQuality(ArtifactQuality.NEW)
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
                .artifactQuality(ArtifactQuality.IMPORTED)
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
