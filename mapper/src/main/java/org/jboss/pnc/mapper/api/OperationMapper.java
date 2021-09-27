/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.mapper.Base32LongIdMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperCentralConfig.class, uses = { RefToReferenceMapper.class, UserMapper.class })
public interface OperationMapper
        extends UpdatableEntityMapper<Base32LongID, Operation, org.jboss.pnc.dto.Operation, OperationRef> {

    Base32LongIdMapper idMapper = new Base32LongIdMapper();

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toDto(dbEntity.getId()) )")
    @Mapping(target = "user", qualifiedBy = Reference.class)
    @Mapping(target = "parameters", source = "operationParameters")
    org.jboss.pnc.dto.Operation toDTO(Operation dbEntity);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toDto(dbEntity.getId()) )")
    @BeanMapping(ignoreUnmappedSourceProperties = { "operationParameters", "user" })
    OperationRef toRef(Operation dbEntity);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toEntity(dtoEntity.getId()) )")
    @Mapping(target = "user", qualifiedBy = IdEntity.class)
    @Mapping(target = "operationParameters", source = "parameters")
    Operation toEntity(org.jboss.pnc.dto.Operation dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "operationParameters", source = "parameters")
    public abstract void updateEntity(org.jboss.pnc.dto.Operation dtoEntity, @MappingTarget Operation target);

    @Override
    default IdMapper<Base32LongID, String> getIdMapper() {
        return idMapper;
    }

}
