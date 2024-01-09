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
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.requests.QValue;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.rest.api.endpoints.ArtifactEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.parameters.PaginationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ArtifactEndpointImpl implements ArtifactEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpointImpl.class);

    private EndpointHelper<Integer, Artifact, ArtifactRef> endpointHelper;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private ProductMilestoneProvider productMilestoneProvider;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Artifact.class, artifactProvider);
    }

    @Override
    public Page<Artifact> getAll(PageParameters pageParams, String sha256, String md5, String sha1) {
        logger.debug(
                "Retrieving Artifacts with these " + pageParams.toString() + "and checksums:"
                        + ((sha256 == null) ? "" : " Sha256: " + sha256) + ((md5 == null) ? "" : " Md5: " + md5)
                        + ((sha1 == null) ? "" : " Sha1: " + sha1));
        return artifactProvider.getAll(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                Optional.ofNullable(sha256),
                Optional.ofNullable(md5),
                Optional.ofNullable(sha1));
    }

    @Override
    public Page<ArtifactInfo> getAllFiltered(
            PaginationParameters paginationParameters,
            String identifier,
            Set<ArtifactQuality> qualities,
            RepositoryType repoType,
            Set<BuildCategory> buildCategories,
            Set<QValue> qualifiers) {
        return artifactProvider.getAllFiltered(
                paginationParameters.getPageIndex(),
                paginationParameters.getPageSize(),
                Optional.ofNullable(identifier),
                qualities,
                Optional.ofNullable(repoType),
                buildCategories,
                qualifiers);
    }

    @Override
    public Artifact getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public Artifact getSpecificFromPurl(String purl) {
        return artifactProvider.getSpecificFromPurl(purl);
    }

    @Override
    public Artifact create(Artifact artifact) {
        return endpointHelper.create(artifact);
    }

    @Override
    public void update(String id, Artifact artifact) {
        endpointHelper.update(id, artifact);
    }

    @Override
    public ArtifactRevision createQualityLevelRevision(String id, String quality, String reason) {
        logger.debug("Updating quality of artifact with id {} to level {} for reason: {}", id, quality, reason);
        return artifactProvider.createQualityLevelRevision(id, quality, reason);
    }

    @Override
    public Page<Build> getDependantBuilds(String id, PageParameters pageParams) {
        return buildProvider.getDependantBuildsForArtifact(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<MilestoneInfo> getMilestonesInfo(String id, PaginationParameters pageParams) {
        return productMilestoneProvider
                .getMilestonesOfArtifact(id, pageParams.getPageIndex(), pageParams.getPageSize());
    }

    @Override
    public Page<ArtifactRevision> getRevisions(String id, @Valid PaginationParameters pageParams) {
        return artifactProvider.getRevisions(pageParams.getPageIndex(), pageParams.getPageSize(), id);
    }

    @Override
    public ArtifactRevision getRevision(String id, int rev) {
        return artifactProvider.getRevision(id, rev);
    }
}
