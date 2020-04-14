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

import org.jboss.pnc.mapper.AbstractArtifactMapper;
import org.jboss.pnc.model.TargetRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(config = MapperCentralConfig.class, uses = { ArtifactMapper.class, AbstractArtifactMapper.IDMapper.class })
public interface TargetRepositoryMapper extends
        EntityMapper<Integer, TargetRepository, org.jboss.pnc.dto.TargetRepository, org.jboss.pnc.dto.TargetRepository> {

    @Override
    @Mapping(target = "artifacts", ignore = true)
    TargetRepository toEntity(org.jboss.pnc.dto.TargetRepository dtoEntity);

    @Override
    @IdEntity
    default TargetRepository toIDEntity(org.jboss.pnc.dto.TargetRepository dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        TargetRepository entity = new TargetRepository();
        entity.setId(Integer.valueOf(dtoEntity.getId()));
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "identifierPath", "artifacts" })
    org.jboss.pnc.dto.TargetRepository toDTO(TargetRepository dbEntity);

    @Override
    @Reference
    default org.jboss.pnc.dto.TargetRepository toRef(TargetRepository dbEntity) {
        return toDTO(dbEntity);
    }

}
