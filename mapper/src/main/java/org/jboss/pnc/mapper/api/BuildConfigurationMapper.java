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

import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper.IDMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = {
                ProjectMapper.class,
                ProductVersionMapper.class,
                EnvironmentMapper.class,
                IDMapper.class,
                SCMRepositoryMapper.class,
                MapSetMapper.class,
                UserMapper.class })
public interface BuildConfigurationMapper
        extends EntityMapper<Integer, BuildConfiguration, org.jboss.pnc.dto.BuildConfiguration, BuildConfigurationRef> {

    @Override
    @Mapping(target = "id", expression = "java( java.lang.Integer.valueOf(dtoEntity.getId()) )")
    @Mapping(target = "lastModificationTime", source = "modificationTime")
    @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildConfigurationSets", source = "groupConfigs")
    @Mapping(target = "repositoryConfiguration", source = "scmRepository", qualifiedBy = IdEntity.class)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "dependants", ignore = true)
    @Mapping(target = "indirectDependencies", ignore = true)
    @Mapping(target = "allDependencies", ignore = true)
    @Mapping(target = "genericParameters", source = "parameters")
    @Mapping(target = "creationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "lastModificationUser", source = "modificationUser", qualifiedBy = IdEntity.class)
    BuildConfiguration toEntity(org.jboss.pnc.dto.BuildConfiguration dtoEntity);

    @Override
    default BuildConfiguration toIDEntity(BuildConfigurationRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        BuildConfiguration entity = new BuildConfiguration();
        entity.setId(Integer.valueOf(dtoEntity.getId()));
        return entity;
    }

    @Override
    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "repositoryConfiguration",
                    "project",
                    "productVersion",
                    "buildEnvironment",
                    "buildConfigurationSets",
                    "dependencies",
                    "indirectDependencies",
                    "allDependencies",
                    "dependants",
                    "currentProductMilestone",
                    "active",
                    "genericParameters",
                    "creationUser",
                    "lastModificationUser" })
    BuildConfigurationRef toRef(BuildConfiguration dbEntity);

    @Override
    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "groupConfigs", source = "buildConfigurationSets")
    @Mapping(target = "dependencies", source = "dependencies")
    @Mapping(target = "scmRepository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "project", resultType = ProjectRef.class)
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "parameters", source = "genericParameters")
    @Mapping(target = "creationUser", qualifiedBy = Reference.class)
    @Mapping(target = "modificationUser", source = "lastModificationUser", qualifiedBy = Reference.class)
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "dependants",
                    "active",
                    "indirectDependencies",
                    "allDependencies",
                    "currentProductMilestone" })
    org.jboss.pnc.dto.BuildConfiguration toDTO(BuildConfiguration dbEntity);

    public static class IDMapper {

        public static Integer toId(BuildConfiguration bc) {
            return bc.getId();
        }

        public static BuildConfiguration toId(Integer bcId) {
            BuildConfiguration bc = new BuildConfiguration();
            bc.setId(bcId);
            return bc;
        }
    }

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
