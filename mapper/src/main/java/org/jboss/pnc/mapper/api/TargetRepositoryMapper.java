/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.TargetRepositoryRef;
import org.jboss.pnc.mapper.AbstractArtifactMapper;
import org.jboss.pnc.model.TargetRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ArtifactMapper.class, AbstractArtifactMapper.IDMapper.class })
public interface TargetRepositoryMapper extends EntityMapper<Integer, TargetRepository, org.jboss.pnc.dto.TargetRepository, TargetRepositoryRef> {
    @Override
    @Mapping(target = "artifacts", source = "artifactIds")
    TargetRepository toEntity(org.jboss.pnc.dto.TargetRepository dtoEntity);

    @Override
    @Mapping(target = "artifacts", ignore = true)
    TargetRepository toIDEntity(TargetRepositoryRef dtoEntity);

    @Override
    @Mapping(target = "artifactIds", source = "artifacts")
    @BeanMapping(ignoreUnmappedSourceProperties = {"identifierPath"})
    org.jboss.pnc.dto.TargetRepository toDTO(TargetRepository dbEntity);

    @Override
    @Reference
    @BeanMapping(ignoreUnmappedSourceProperties = {"artifacts", "identifierPath"})
    TargetRepositoryRef toRef(TargetRepository dbEntity);


}
