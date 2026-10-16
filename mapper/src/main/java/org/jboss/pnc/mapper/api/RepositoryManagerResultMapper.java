/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.RepositoryManagerResultRest;
import org.jboss.pnc.spi.repositorymanager.DefaultRepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { ArtifactMapper.class })
public interface RepositoryManagerResultMapper
        extends SimpleMapper<RepositoryManagerResultRest, RepositoryManagerResult> {

    @Override
    @Mapping(target = "builtArtifacts", source = "builtArtifacts", qualifiedBy = TransientTargetRepo.class)
    @Mapping(target = "dependencies", source = "dependencies", qualifiedBy = TransientTargetRepo.class)
    @BeanMapping(resultType = DefaultRepositoryManagerResult.class)
    RepositoryManagerResult toEntity(RepositoryManagerResultRest repositoryManagerResultRest);

    @Override
    RepositoryManagerResultRest toDTO(RepositoryManagerResult entity);
}
