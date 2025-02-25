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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.validation.DTOValidationException;

import java.util.Optional;
import java.util.Set;

public interface ArtifactProvider
        extends Provider<Integer, org.jboss.pnc.model.Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef> {
    Page<Artifact> getAll(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Optional<String> sha256,
            Optional<String> md5,
            Optional<String> sha1);

    Page<ArtifactInfo> getAllFiltered(
            int pageIndex,
            int pageSize,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities, // default value is empty Set
            Optional<RepositoryType> repoType,
            Set<BuildCategory> buildCategories // default value is empty Set
    );

    Page<Artifact> getBuiltArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId);

    Page<Artifact> getArtifactsForTargetRepository(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Integer targetRepositoryId);

    Page<Artifact> getDependantArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId);

    Page<Artifact> getDeliveredArtifactsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String milestoneId);

    Page<ArtifactRevision> getRevisions(int pageIndex, int pageSize, String id);

    ArtifactRevision getRevision(String id, Integer rev);

    ArtifactRevision createQualityLevelRevision(String id, String quality, String reason) throws DTOValidationException;

    Artifact getSpecificFromPurl(String purl);

    Page<Artifact> getDeliveredArtifactsSharedInMilestones(
            int pageIndex,
            int pageSize,
            String sort,
            String q,
            String milestone1Id,
            String milestone2Id);
}
