/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
    @Mapping(target = "outcome.result", source = "result")
    @Mapping(target = "outcome.reason", source = "reason")
    @Mapping(target = "outcome.proposal", source = "proposal")
    org.jboss.pnc.dto.Operation toDTO(Operation dbEntity);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toDto(dbEntity.getId()) )")
    @Mapping(target = "outcome.result", source = "result")
    @Mapping(target = "outcome.reason", source = "reason")
    @Mapping(target = "outcome.proposal", source = "proposal")
    @BeanMapping(ignoreUnmappedSourceProperties = { "operationParameters", "user" })
    OperationRef toRef(Operation dbEntity);

    @Override
    @Mapping(target = "id", expression = "java( getIdMapper().toEntity(dtoEntity.getId()) )")
    @Mapping(target = "user", qualifiedBy = IdEntity.class)
    @Mapping(target = "operationParameters", source = "parameters")
    @Mapping(target = ".", source = "outcome")
    Operation toEntity(org.jboss.pnc.dto.Operation dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "operationParameters", source = "parameters")
    @Mapping(target = ".", source = "outcome")
    public abstract void updateEntity(org.jboss.pnc.dto.Operation dtoEntity, @MappingTarget Operation target);

    @Override
    default IdMapper<Base32LongID, String> getIdMapper() {
        return idMapper;
    }
}
