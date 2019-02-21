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

import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {ProjectMapper.class, EnvironmentMapper.class, SCMRepositoryMapper.class}, imports = IdRev.class)
public interface BuildConfigurationRevisionMapper {

     @Mapping(target = "repository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
     @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
     @Mapping(target = "project", resultType = ProjectRef.class)
     @Mapping(target = "modificationTime", source = "lastModificationTime")
     @BeanMapping(ignoreUnmappedSourceProperties = {"idRev", "buildRecords", "buildConfiguration"})
     BuildConfigurationRevision toDTO(BuildConfigurationAudited dbEntity);

     default BuildConfigurationAudited toIDEntity(BuildConfigurationRevisionRef dtoEntity) {
         BuildConfigurationAudited entity = new BuildConfigurationAudited();
         entity.setId(dtoEntity.getId());
         entity.setRev(dtoEntity.getRev());
         return entity;
     };

     @Mapping(target = "repositoryConfiguration", source = "repository", qualifiedBy = IdEntity.class)
     @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
     @Mapping(target = "idRev", expression = "java( new IdRev( dtoEntity.getId(), dtoEntity.getRev() ) )")
     @Mapping(target = "buildRecords", ignore = true)
     @Mapping(target = "buildConfiguration", ignore = true)
     @Mapping(target = "lastModificationTime", source = "modificationTime")
     BuildConfigurationAudited toEntity(BuildConfigurationRevision dtoEntity);

     @Mapping(target = "modificationTime", source = "lastModificationTime")
     @BeanMapping(ignoreUnmappedSourceProperties = {"idRev", "buildRecords", "buildConfiguration",
             "repositoryConfiguration", "buildEnvironment", "project", "genericParameters"
     })
     BuildConfigurationRevisionRef toRef(BuildConfigurationAudited dbEntity);
}
