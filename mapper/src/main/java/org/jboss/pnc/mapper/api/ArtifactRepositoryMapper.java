/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.ArtifactRepositoryRest;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.DefaultArtifactRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */

@Mapper(config = MapperCentralConfig.class)
public interface ArtifactRepositoryMapper extends SimpleMapper<ArtifactRepositoryRest, ArtifactRepository> {

    @Override
    @BeanMapping(resultType = DefaultArtifactRepository.class)
    ArtifactRepository toEntity(ArtifactRepositoryRest artifactRepositoryRest);

    @Override
    ArtifactRepositoryRest toDTO(ArtifactRepository entity);
}
