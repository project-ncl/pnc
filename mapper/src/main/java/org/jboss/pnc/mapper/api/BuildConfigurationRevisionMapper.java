/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { RefToReferenceMapper.class, ProjectMapper.class, EnvironmentMapper.class, SCMRepositoryMapper.class,
                UserMapper.class, MapSetMapper.class },
        imports = IdRev.class)
public interface BuildConfigurationRevisionMapper {

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "scmRepository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "project", resultType = ProjectRef.class)
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @Mapping(target = "parameters", source = "genericParameters")
    @Mapping(target = "creationUser", qualifiedBy = Reference.class)
    @Mapping(target = "alignmentConfigs", source = "alignConfigs")
    @Mapping(target = "modificationUser", source = "lastModificationUser", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "idRev", "buildConfiguration" })
    BuildConfigurationRevision toDTO(BuildConfigurationAudited dbEntity);

    @Mapping(target = "repositoryConfiguration", source = "scmRepository", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
    @Mapping(
            target = "idRev",
            expression = "java( new IdRev( Integer.valueOf(dtoEntity.getId()), dtoEntity.getRev() ) )")
    @Mapping(target = "buildConfiguration", ignore = true)
    @Mapping(target = "lastModificationTime", source = "modificationTime")
    @Mapping(target = "genericParameters", source = "parameters")
    @Mapping(target = "alignConfigs", source = "alignmentConfigs")
    @Mapping(target = "creationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "lastModificationUser", source = "modificationUser", qualifiedBy = IdEntity.class)
    BuildConfigurationAudited toEntity(BuildConfigurationRevision dtoEntity);

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "idRev", "buildConfiguration", "repositoryConfiguration",
                    "buildEnvironment", "project", "genericParameters", "creationUser", "lastModificationUser" })
    BuildConfigurationRevisionRef toRef(BuildConfigurationAudited dbEntity);
}
