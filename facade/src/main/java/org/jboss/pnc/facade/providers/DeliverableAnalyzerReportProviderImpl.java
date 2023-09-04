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

import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerReportProvider;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.EntityMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@PermitAll
@Stateless
public class DeliverableAnalyzerReportProviderImpl extends
        AbstractProvider<Base32LongID, DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport>
        implements DeliverableAnalyzerReportProvider {

    private final DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    private final ArtifactMapper artifactMapper;

    @Inject
    public DeliverableAnalyzerReportProviderImpl(
            Repository<DeliverableAnalyzerReport, Base32LongID> repository,
            EntityMapper<Base32LongID, DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport> mapper,
            Class<DeliverableAnalyzerReport> type,
            DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository,
            ArtifactMapper artifactMapper) {
        super(repository, mapper, type);
        this.deliverableAnalyzerReportRepository = deliverableAnalyzerReportRepository;
        this.artifactMapper = artifactMapper;
    }

    @Override
    public Page<AnalyzedArtifact> getAnalyzedArtifacts(int pageIndex, int pageSize, String id) {
        Base32LongID entityId = mapper.getIdMapper().toEntity(id);

        List<AnalyzedArtifact> analyzedArtifacts = deliverableAnalyzerReportRepository.getAnalyzedArtifacts(entityId)
                .stream()
                .map(this::deliverableArtifactToDto)
                .collect(Collectors.toList());

        return new Page<>(
                pageIndex,
                pageSize,
                deliverableAnalyzerReportRepository.countAnalyzedArtifacts(entityId),
                analyzedArtifacts);
    }

    private AnalyzedArtifact deliverableArtifactToDto(DeliverableArtifact deliverableArtifact) {
        return AnalyzedArtifact.builder()
                .builtFromSource(deliverableArtifact.isBuiltFromSource())
                .brewId(deliverableArtifact.getBrewBuildId())
                .artifact(artifactMapper.toDTO(deliverableArtifact.getArtifact()))
                .build();
    }
}
