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

import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerReportProvider;
import org.jboss.pnc.facade.util.DeliverableAnalyzerReportLabelModifier;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerReportMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.spi.datastore.predicates.DeliverableArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerLabelEntryPredicates.withReportId;

@PermitAll
@Stateless
public class DeliverableAnalyzerReportProviderImpl extends
        AbstractProvider<Base32LongID, DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport>
        implements DeliverableAnalyzerReportProvider {

    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    private DeliverableArtifactRepository deliverableArtifactRepository;

    private DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;

    private DeliverableAnalyzerReportMapper deliverableAnalyzerReportMapper;

    private UserService userService;

    private ArtifactMapper artifactMapper;

    private UserMapper userMapper;

    private DeliverableAnalyzerReportLabelModifier labelModifier;

    @Inject
    public DeliverableAnalyzerReportProviderImpl(
            DeliverableAnalyzerReportRepository repository,
            DeliverableArtifactRepository deliverableArtifactRepository,
            DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository,
            UserService userService,
            DeliverableAnalyzerReportMapper mapper,
            ArtifactMapper artifactMapper,
            UserMapper userMapper,
            DeliverableAnalyzerReportLabelModifier labelModifier) {
        super(repository, mapper, DeliverableAnalyzerReport.class);

        this.deliverableAnalyzerReportRepository = repository;
        this.deliverableArtifactRepository = deliverableArtifactRepository;
        this.deliverableAnalyzerLabelEntryRepository = deliverableAnalyzerLabelEntryRepository;
        this.userService = userService;
        this.deliverableAnalyzerReportMapper = mapper;
        this.artifactMapper = artifactMapper;
        this.userMapper = userMapper;
        this.labelModifier = labelModifier;
    }

    @Override
    public Page<AnalyzedArtifact> getAnalyzedArtifacts(
            int pageIndex,
            int pageSize,
            String query,
            String sort,
            String id) {
        Base32LongID entityId = transformToEntityId(id);
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

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void addLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        Base32LongID reportId = transformToEntityId(id);
        DeliverableAnalyzerReport report = deliverableAnalyzerReportRepository.queryById(reportId);
        DeliverableAnalyzerLabelEntry labelHistoryEntry = DeliverableAnalyzerLabelEntry.builder()
                .report(report)
                .changeOrder(deliverableAnalyzerLabelEntryRepository.getLatestChangeOrderOfReport(report.getId()))
                .entryTime(Date.from(Instant.now()))
                .user(userService.currentUser())
                .reason(request.getReason())
                .label(request.getLabel())
                .change(LabelOperation.ADDED)
                .build();

        labelModifier.addLabelToActiveLabelsAndModifyLabelHistory(
                reportId,
                request.getLabel(),
                report.getLabels(),
                labelHistoryEntry);
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void removeLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        Base32LongID reportId = transformToEntityId(id);
        DeliverableAnalyzerReport report = deliverableAnalyzerReportRepository.queryById(reportId);
        DeliverableAnalyzerLabelEntry labelHistoryEntry = DeliverableAnalyzerLabelEntry.builder()
                .report(report)
                .changeOrder(deliverableAnalyzerLabelEntryRepository.getLatestChangeOrderOfReport(report.getId()))
                .entryTime(Date.from(Instant.now()))
                .user(userService.currentUser())
                .reason(request.getReason())
                .label(request.getLabel())
                .change(LabelOperation.REMOVED)
                .build();

        labelModifier.removeLabelFromActiveLabelsAndModifyLabelHistory(
                reportId,
                request.getLabel(),
                report.getLabels(),
                labelHistoryEntry);
    }

    @Override
    public Page<org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry> getLabelHistory(
            String id,
            int pageIndex,
            int pageSize,
            String sort,
            String query) {
        var reportId = transformToEntityId(id);
        Predicate<DeliverableAnalyzerLabelEntry> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(DeliverableAnalyzerLabelEntry.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo<DeliverableAnalyzerLabelEntry> sortInfo = rsqlPredicateProducer
                .getSortInfo(DeliverableAnalyzerLabelEntry.class, sort);

        List<org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry> labelHistory = deliverableAnalyzerLabelEntryRepository
                .queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withReportId(reportId))
                .stream()
                .map(this::deliverableAnalyzerLabelEntryToDto)
                .collect(Collectors.toList());
        int totalHits = deliverableAnalyzerLabelEntryRepository.count(rsqlPredicate, withReportId(reportId));

        return new Page<>(pageIndex, pageSize, totalHits, labelHistory);
    }

    private Base32LongID transformToEntityId(String id) {
        return mapper.getIdMapper().toEntity(id);
    }

    private org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry deliverableAnalyzerLabelEntryToDto(
            DeliverableAnalyzerLabelEntry deliverableAnalyzerLabelEntry) {
        return org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry.builder()
                .label(deliverableAnalyzerLabelEntry.getLabel())
                .date(deliverableAnalyzerLabelEntry.getEntryTime())
                .reason(deliverableAnalyzerLabelEntry.getReason())
                .user(userMapper.toDTO(deliverableAnalyzerLabelEntry.getUser()))
                .build();
    }

    private AnalyzedArtifact deliverableArtifactToDto(DeliverableArtifact deliverableArtifact) {
        return AnalyzedArtifact.builder()
                .builtFromSource(deliverableArtifact.isBuiltFromSource())
                .brewId(deliverableArtifact.getBrewBuildId())
                .artifact(artifactMapper.toDTO(deliverableArtifact.getArtifact()))
                .build();
    }
}
