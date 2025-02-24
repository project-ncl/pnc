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

import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.mapper.Base32LongIdMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushReport;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperCentralConfig.class, uses = { UserMapper.class, BuildMapper.class })
public interface BuildPushReportMapper extends
        EntityMapper<Base32LongID, BuildPushReport, org.jboss.pnc.dto.BuildPushReport, org.jboss.pnc.dto.BuildPushReport> {

    Base32LongIdMapper idMapper = new Base32LongIdMapper();

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toEntity(dtoEntity.getId()) )")
    @Mapping(target = "operation.submitTime", source = "submitTime")
    @Mapping(target = "operation.startTime", source = "startTime")
    @Mapping(target = "operation.endTime", source = "endTime")
    @Mapping(target = "operation.user", source = "user")
    @Mapping(
            target = "operation.operationParameters",
            expression = "java( java.util.Map.of(org.jboss.pnc.api.constants.OperationParameters.BUILD_PUSH_TAG_PREFIX, buildPushReport.getTagPrefix()) )")
    @Mapping(target = "operation.build", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id" })
    BuildPushReport toEntity(org.jboss.pnc.dto.BuildPushReport dtoEntity);

    @Mapping(target = "id", expression = "java( getIdMapper().toDto(operation.getId()) )")
    @Mapping(
            target = "tagPrefix",
            expression = "java( operation.getOperationParameters().get(org.jboss.pnc.api.constants.OperationParameters.BUILD_PUSH_TAG_PREFIX) )")
    @Mapping(target = "build", source = "build", resultType = BuildRef.class)
    @Mapping(target = "brewBuildId", ignore = true)
    @Mapping(target = "brewBuildUrl", ignore = true)
    org.jboss.pnc.dto.BuildPushReport fromOperation(BuildPushOperation operation);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toDto(dbEntity.getId()) )")
    @Mapping(target = "submitTime", source = "dbEntity.operation.submitTime")
    @Mapping(target = "startTime", source = "dbEntity.operation.startTime")
    @Mapping(target = "endTime", source = "dbEntity.operation.endTime")
    @Mapping(target = "user", source = "dbEntity.operation.user")
    @Mapping(target = "result", source = "dbEntity.operation.result")
    @Mapping(target = "tagPrefix", source = "dbEntity.operation.tagPrefix")
    @Mapping(target = "build", source = "dbEntity.operation.build", resultType = BuildRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id" })
    org.jboss.pnc.dto.BuildPushReport toDTO(BuildPushReport dbEntity);

    @Override
    @Reference
    default org.jboss.pnc.dto.BuildPushReport toRef(BuildPushReport dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    default IdMapper<Base32LongID, String> getIdMapper() {
        return idMapper;
    }
}
