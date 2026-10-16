/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Optional;
import java.util.Set;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.validation.DTOValidationException;

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
