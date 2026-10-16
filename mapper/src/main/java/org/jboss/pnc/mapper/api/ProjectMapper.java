/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.model.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { MapSetMapper.class })
public interface ProjectMapper extends UpdatableEntityMapper<Integer, Project, org.jboss.pnc.dto.Project, ProjectRef> {

    @Override
    @Mapping(target = "buildConfigs", source = "buildConfigurations")
    org.jboss.pnc.dto.Project toDTO(Project dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "buildConfigurations" })
    ProjectRef toRef(Project dbEntity);

    @Override
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    Project toEntity(org.jboss.pnc.dto.Project dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "buildConfigurations", ignore = true)
    void updateEntity(org.jboss.pnc.dto.Project dtoEntity, @MappingTarget Project target);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
