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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.api.deliverablesanalyzer.dto.LicenseInfo;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.AnalyzedDistribution;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerReportProvider;
import org.jboss.pnc.facade.util.labels.DeliverableAnalyzerLabelSaver;
import org.jboss.pnc.facade.util.labels.DeliverableAnalyzerReportLabelModifier;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerLabelEntryMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerReportMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactLicenseInfo;
import org.jboss.pnc.spi.datastore.predicates.DeliverableArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerLabelEntryPredicates.withReportId;

@PermitAll
@Stateless
public class DeliverableAnalyzerReportProviderImpl extends
        AbstractProvider<Base32LongID, DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport>
        implements DeliverableAnalyzerReportProvider {

    private final DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    private final DeliverableArtifactRepository deliverableArtifactRepository;

    private final DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;

    private final DeliverableAnalyzerLabelEntryMapper deliverableAnalyzerLabelEntryMapper;

    private final ArtifactMapper artifactMapper;

    private final DeliverableAnalyzerReportLabelModifier labelModifier;

    private final DeliverableAnalyzerLabelSaver labelSaver;

    @Inject
    public DeliverableAnalyzerReportProviderImpl(
            DeliverableAnalyzerReportRepository repository,
            DeliverableArtifactRepository deliverableArtifactRepository,
            DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository,
            DeliverableAnalyzerReportMapper mapper,
            DeliverableAnalyzerLabelEntryMapper deliverableAnalyzerLabelEntryMapper,
            ArtifactMapper artifactMapper,
            DeliverableAnalyzerReportLabelModifier labelModifier,
            DeliverableAnalyzerLabelSaver labelSaver) {
        super(repository, mapper, DeliverableAnalyzerReport.class);

        this.deliverableAnalyzerReportRepository = repository;
        this.deliverableArtifactRepository = deliverableArtifactRepository;
        this.deliverableAnalyzerLabelEntryRepository = deliverableAnalyzerLabelEntryRepository;
        this.deliverableAnalyzerLabelEntryMapper = deliverableAnalyzerLabelEntryMapper;
        this.artifactMapper = artifactMapper;
        this.labelModifier = labelModifier;
        this.labelSaver = labelSaver;
    }

    @Override
    public Page<AnalyzedArtifact> getAnalyzedArtifacts(
            int pageIndex,
            int pageSize,
            String query,
            String sort,
            String id) {
        Base32LongID entityId = parseId(id);
        Predicate<DeliverableArtifact> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(DeliverableArtifact.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo<DeliverableArtifact> sortInfo = rsqlPredicateProducer.getSortInfo(DeliverableArtifact.class, sort);

        List<AnalyzedArtifact> analyzedArtifacts = deliverableArtifactRepository
                .queryWithPredicates(
                        pageInfo,
                        sortInfo,
                        rsqlPredicate,
                        DeliverableArtifactPredicates.withReportId(entityId))
                .stream()
                .map(this::deliverableArtifactToDto)
                .collect(Collectors.toList());

        return new Page<>(
                pageIndex,
                pageSize,
                deliverableArtifactRepository
                        .count(rsqlPredicate, DeliverableArtifactPredicates.withReportId(entityId)),
                analyzedArtifacts);
    }

    @Override
    public Page<org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry> getLabelHistory(
            String id,
            int pageIndex,
            int pageSize,
            String sort,
            String query) {
        Base32LongID reportId = parseId(id);
        Predicate<DeliverableAnalyzerLabelEntry> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(DeliverableAnalyzerLabelEntry.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo<DeliverableAnalyzerLabelEntry> sortInfo = rsqlPredicateProducer
                .getSortInfo(DeliverableAnalyzerLabelEntry.class, sort);

        List<org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry> labelHistory = deliverableAnalyzerLabelEntryRepository
                .queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withReportId(reportId))
                .stream()
                .map(deliverableAnalyzerLabelEntryMapper::toDto)
                .collect(Collectors.toList());
        int totalHits = deliverableAnalyzerLabelEntryRepository.count(rsqlPredicate, withReportId(reportId));

        return new Page<>(pageIndex, pageSize, totalHits, labelHistory);
    }

    @Override
    public void addLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        Base32LongID reportId = parseId(id);
        DeliverableAnalyzerReport report = deliverableAnalyzerReportRepository.queryById(reportId);
        if (report == null) {
            throw new EmptyEntityException("Deliverable analyzer report with id: " + id + " does not exist!");
        }

        labelSaver.init(report, request.getReason());
        labelModifier.validateAndAddLabel(request.getLabel(), report.getLabels());
    }

    @Override
    public void removeLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        Base32LongID reportId = parseId(id);
        DeliverableAnalyzerReport report = deliverableAnalyzerReportRepository.queryById(reportId);
        if (report == null) {
            throw new EmptyEntityException("Deliverable analyzer report with id: " + id + " does not exist!");
        }

        labelSaver.init(report, request.getReason());
        labelModifier.validateAndRemoveLabel(request.getLabel(), report.getLabels());
    }

    private AnalyzedArtifact deliverableArtifactToDto(DeliverableArtifact deliverableArtifact) {
        AnalyzedDistribution distribution = deliverableArtifact.getDistribution() != null
                ? AnalyzedDistribution.builder()
                        .distributionUrl(deliverableArtifact.getDistribution().getDistributionUrl())
                        .creationTime(deliverableArtifact.getDistribution().getCreationTime())
                        .md5(deliverableArtifact.getDistribution().getMd5())
                        .sha1(deliverableArtifact.getDistribution().getSha1())
                        .sha256(deliverableArtifact.getDistribution().getSha256())
                        .build()
                : null;

        Set<LicenseInfo> licenses = Optional.ofNullable(deliverableArtifact.getLicenses())
                .orElse(Collections.emptySet())
                .stream()
                .map((DeliverableArtifactLicenseInfo license) -> {
                    return LicenseInfo.builder()
                            .comments(license.getComments())
                            .distribution(license.getDistribution())
                            .name(license.getName())
                            .spdxLicenseId(license.getSpdxLicenseId())
                            .url(license.getUrl())
                            .sourceUrl(license.getSourceUrl())
                            .build();
                })
                .collect(Collectors.toSet());

        return AnalyzedArtifact.builder()
                .builtFromSource(deliverableArtifact.isBuiltFromSource())
                .brewId(deliverableArtifact.getBrewBuildId())
                .artifact(artifactMapper.toDTO(deliverableArtifact.getArtifact()))
                .archiveFilenames(StringUtils.splitString(deliverableArtifact.getArchiveFilenames()))
                .archiveUnmatchedFilenames(StringUtils.splitString(deliverableArtifact.getArchiveUnmatchedFilenames()))
                .distribution(distribution)
                .licenses(licenses)
                .build();
    }
}
