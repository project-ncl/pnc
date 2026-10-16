/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.TargetRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(config = MapperCentralConfig.class)
public interface TargetRepositoryMapper extends
        EntityMapper<Integer, TargetRepository, org.jboss.pnc.dto.TargetRepository, org.jboss.pnc.dto.TargetRepository> {

    @Override
    @Mapping(target = "artifacts", ignore = true)
    TargetRepository toEntity(org.jboss.pnc.dto.TargetRepository dtoEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "identifierPath", "artifacts" })
    org.jboss.pnc.dto.TargetRepository toDTO(TargetRepository dbEntity);

    @Override
    @Reference
    default org.jboss.pnc.dto.TargetRepository toRef(TargetRepository dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
