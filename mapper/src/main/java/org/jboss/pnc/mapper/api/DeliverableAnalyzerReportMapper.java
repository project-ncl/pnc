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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.mapper.Base32LongIdMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperCentralConfig.class, uses = { UserMapper.class, ProductMilestoneMapper.class })
public interface DeliverableAnalyzerReportMapper extends
        EntityMapper<Base32LongID, DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport, org.jboss.pnc.dto.DeliverableAnalyzerReport> {

    Base32LongIdMapper idMapper = new Base32LongIdMapper();

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toEntity(dtoEntity.getId()) )")
    @Mapping(target = "operation.submitTime", source = "submitTime")
    @Mapping(target = "operation.startTime", source = "startTime")
    @Mapping(target = "operation.endTime", source = "endTime")
    @Mapping(target = "operation.user", source = "user")
    @Mapping(
            target = "operation.operationParameters",
            expression = "java( UrlTreeSetAndOperationParametersConverter.operationParametersFromUrlSet(deliverableAnalyzerReport.getUrls()) )")
    @Mapping(target = "operation.productMilestone", source = "productMilestone")
    @Mapping(target = "labelHistory", ignore = true)
    @Mapping(target = "artifacts", ignore = true)
    @Mapping(target = "operation.productMilestone.productVersion", ignore = true)
    @Mapping(target = "operation.productMilestone.performedBuilds", ignore = true)
    @Mapping(target = "operation.productMilestone.deliveredArtifacts", ignore = true)
    @Mapping(target = "operation.productMilestone.deliveredArtifactsImporter", ignore = true)
    @Mapping(target = "operation.productMilestone.productRelease", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id" })
    DeliverableAnalyzerReport toEntity(org.jboss.pnc.dto.DeliverableAnalyzerReport dtoEntity);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toDto(dbEntity.getId()) )")
    @Mapping(target = "submitTime", source = "dbEntity.operation.submitTime")
    @Mapping(target = "startTime", source = "dbEntity.operation.startTime")
    @Mapping(target = "endTime", source = "dbEntity.operation.endTime")
    @Mapping(target = "user", source = "dbEntity.operation.user")
    @Mapping(
            target = "urls",
            expression = "java( UrlTreeSetAndOperationParametersConverter.urlSetFromOperationParameters(dbEntity.getOperation().getOperationParameters()) )")
    @Mapping(
            target = "productMilestone",
            source = "dbEntity.operation.productMilestone",
            resultType = ProductMilestoneRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id", "artifacts", "labelHistory" })
    org.jboss.pnc.dto.DeliverableAnalyzerReport toDTO(DeliverableAnalyzerReport dbEntity);

    @Override
    @Reference
    default org.jboss.pnc.dto.DeliverableAnalyzerReport toRef(DeliverableAnalyzerReport dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    default IdMapper<Base32LongID, String> getIdMapper() {
        return idMapper;
    }
}
