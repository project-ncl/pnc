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
package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.facade.mapper.api.BuildConfigurationMapper.IDMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {ProjectMapper.class, ProductVersionMapper.class, EnvironmentMapper.class,
            IDMapper.class, SCMRepositoryMapper.class, GroupConfigurationMapper.class})
public interface BuildConfigurationMapper extends EntityMapper<BuildConfiguration, org.jboss.pnc.dto.BuildConfiguration, BuildConfigurationRef> {

    @Override
    @Mapping(target = "lastModificationTime", source = "modificationTime")
    @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildConfigurationSets", source = "groupConfigs")
    @Mapping(target = "dependencies", source = "dependencyIds")
    @Mapping(target = "repositoryConfiguration", source = "repository", qualifiedBy = IdEntity.class)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "dependants", ignore = true)
    @Mapping(target = "indirectDependencies", ignore = true)
    @Mapping(target = "allDependencies", ignore = true)
    BuildConfiguration toEntity(org.jboss.pnc.dto.BuildConfiguration dtoEntity);

    @Override
    default BuildConfiguration toIDEntity(BuildConfigurationRef dtoEntity) {
        BuildConfiguration entity = new BuildConfiguration();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @BeanMapping(ignoreUnmappedSourceProperties = {"repositoryConfiguration", "project",
        "productVersion", "buildEnvironment", "buildConfigurationSets", "dependencies",
        "indirectDependencies", "allDependencies", "dependants", "currentProductMilestone", "active",
        "genericParameters"})
    BuildConfigurationRef toRef(BuildConfiguration dbEntity);


    @Override
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "groupConfigs", source = "buildConfigurationSets", resultType = GroupConfigurationRef.class)
    @Mapping(target = "dependencyIds", source = "dependencies")
    @Mapping(target = "repository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "project", resultType = ProjectRef.class)
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"dependants", "active", "indirectDependencies",
        "allDependencies", "currentProductMilestone"})
    org.jboss.pnc.dto.BuildConfiguration toDTO(BuildConfiguration dbEntity);

    public static class IDMapper {

        public Integer toId(BuildConfiguration bc) {
            return bc.getId();
        }

        public BuildConfiguration toId(Integer bcId) {
            BuildConfiguration bc = new BuildConfiguration();
            bc.setId(bcId);
            return bc;
        }
    }
}
