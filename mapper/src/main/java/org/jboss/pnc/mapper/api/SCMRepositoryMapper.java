/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface SCMRepositoryMapper
        extends UpdatableEntityMapper<Integer, RepositoryConfiguration, SCMRepository, SCMRepository> {

    @Override
    @Mapping(target = "internalUrlNormalized", ignore = true)
    @Mapping(target = "externalUrlNormalized", ignore = true)
    @Mapping(target = "buildConfigurations", ignore = true)
    @Mapping(target = "preBuildSyncEnabled", defaultValue = "true")
    RepositoryConfiguration toEntity(SCMRepository dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "internalUrl", ignore = true)
    @Mapping(target = "internalUrlNormalized", ignore = true)
    @Mapping(target = "externalUrlNormalized", ignore = true)
    @Mapping(target = "buildConfigurations", ignore = true)
    void updateEntity(SCMRepository dtoEntity, @MappingTarget RepositoryConfiguration target);

    @Override
    @Reference
    default SCMRepository toRef(RepositoryConfiguration dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "internalUrlNormalized",
                    "externalUrlNormalized",
                    "buildConfigurations" })
    SCMRepository toDTO(RepositoryConfiguration dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
