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

import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.model.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { MapSetMapper.class })
public interface ProjectMapper extends EntityMapper<Integer, Project, org.jboss.pnc.dto.Project, ProjectRef> {

    @Override
    @Mapping(target = "buildConfigs", source = "buildConfigurations")
    org.jboss.pnc.dto.Project toDTO(Project dbEntity);

    @Override
    default Project toIDEntity(ProjectRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        Project entity = new Project();
        entity.setId(Integer.valueOf(dtoEntity.getId()));
        return entity;
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "buildConfigurations" })
    ProjectRef toRef(Project dbEntity);

    @Override
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    Project toEntity(org.jboss.pnc.dto.Project dtoEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
