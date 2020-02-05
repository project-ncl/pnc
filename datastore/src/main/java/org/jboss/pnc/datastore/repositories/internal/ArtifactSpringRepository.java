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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository.RawArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import javax.enterprise.context.Dependent;

@Dependent
public interface ArtifactSpringRepository extends JpaRepository<Artifact, Integer>, JpaSpecificationExecutor<Artifact> {

    @Query(
            value = "SELECT DISTINCT " +
                    " artifact.id, " +
                    " artifact.artifactQuality, " +
                    " artifact.deployPath, " +
                    " artifact.filename, " +
                    " artifact.identifier, " +
                    " artifact.importDate, " +
                    " artifact.md5, " +
                    " artifact.originUrl, " +
                    " artifact.sha1, " +
                    " artifact.sha256, " +
                    " artifact.size, " +
                    " artifact.targetRepository_id as targetRepositoryId, " +
                    " targetRepository.temporaryRepo, " +
                    " targetRepository.identifier as targetRepositoryIdentifier, " +
                    " targetRepository.repositoryPath, " +
                    " targetRepository.repositoryType " +
                    "FROM Artifact artifact " +
                    "INNER JOIN build_record_artifact_dependencies_map dependency_artifact " +
                    "  ON dependency_artifact.dependency_artifact_id = artifact.id " +
                    "INNER JOIN BuildRecord buildRecord " +
                    "  ON dependency_artifact.build_record_id = buildRecord.id " +
                    "INNER JOIN TargetRepository targetRepository " +
                    "  ON targetRepository.id = artifact.targetRepository_id " +
                    "WHERE " +
                    " buildRecord.id = ?1 " +
                    "ORDER BY " +
                    " artifact.id ASC LIMIT ?2 OFFSET ?3",
            nativeQuery = true)
    List<RawArtifact> getMinimizedDependencyArtifactsForBuildRecord(Integer buildRecordId, int pageSize, int offset);

    @Query(
            value = "SELECT COUNT(DISTINCT artifact.id) " +
                    "FROM Artifact artifact " +
                    "INNER JOIN build_record_artifact_dependencies_map dependency_artifact " +
                    "  ON dependency_artifact.dependency_artifact_id = artifact.id " +
                    "INNER JOIN BuildRecord buildRecord " +
                    "  ON dependency_artifact.build_record_id = buildRecord.id " +
                    "INNER JOIN TargetRepository targetRepository " +
                    "  ON targetRepository.id = artifact.targetRepository_id " +
                    "WHERE " +
                    " buildRecord.id = ?1",
            nativeQuery = true)
    Object[] countMinimizedDependencyArtifactsForBuildRecord(Integer buildRecordId);

    @Query(
            value = "SELECT DISTINCT " +
                    " artifact.id, " +
                    " artifact.artifactQuality, " +
                    " artifact.deployPath, " +
                    " artifact.filename, " +
                    " artifact.identifier, " +
                    " artifact.importDate, " +
                    " artifact.md5, " +
                    " artifact.originUrl, " +
                    " artifact.sha1, " +
                    " artifact.sha256, " +
                    " artifact.size, " +
                    " artifact.targetRepository_id as targetRepositoryId, " +
                    " targetRepository.temporaryRepo, " +
                    " targetRepository.identifier as targetRepositoryIdentifier, " +
                    " targetRepository.repositoryPath, " +
                    " targetRepository.repositoryType " +
                    "FROM Artifact artifact " +
                    "INNER JOIN build_record_built_artifact_map built_artifact " +
                    "  ON built_artifact.built_artifact_id = artifact.id " +
                    "INNER JOIN BuildRecord buildRecord " +
                    "  ON built_artifact.build_record_id = buildRecord.id " +
                    "INNER JOIN TargetRepository targetRepository " +
                    "  ON targetRepository.id = artifact.targetRepository_id " +
                    "WHERE " +
                    " buildRecord.id = ?1 " +
                    "ORDER BY " +
                    " artifact.id ASC LIMIT ?2 OFFSET ?3",
            nativeQuery = true)
    List<RawArtifact> getMinimizedBuiltArtifactsForBuildRecord(Integer buildRecordId, int pageSize, int offset);

    @Query(
            value = "SELECT COUNT(DISTINCT artifact.id) " +
                    "FROM Artifact artifact " +
                    "INNER JOIN build_record_built_artifact_map built_artifact " +
                    "  ON built_artifact.built_artifact_id = artifact.id " +
                    "INNER JOIN BuildRecord buildRecord " +
                    "  ON built_artifact.build_record_id = buildRecord.id " +
                    "INNER JOIN TargetRepository targetRepository " +
                    "  ON targetRepository.id = artifact.targetRepository_id " +
                    "WHERE " +
                    " buildRecord.id = ?1",
            nativeQuery = true)
    Object[] countMinimizedBuiltArtifactsForBuildRecord(Integer buildRecordId);

}