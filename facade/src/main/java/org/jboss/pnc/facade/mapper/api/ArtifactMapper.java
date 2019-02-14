/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.model.Artifact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {BuildConfigurationMapper.class, TargetRepositoryMapper.class, BuildMapper.IDMapper.class})
public interface ArtifactMapper extends EntityMapper<Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>{

    @Override
    @Mapping(target = "buildIds", source = "buildRecords")
    @Mapping(target = "dependantBuildIds", source = "dependantBuildRecords")
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @Mapping(target = "targetRepository", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = {"distributedInProductMilestones",
            "identifierSha256", "built", "imported", "trusted", "descriptiveString"
    })
    org.jboss.pnc.dto.Artifact toDTO(Artifact dbEntity);

    @Override
    default Artifact toIDEntity(ArtifactRef dtoEntity) {
        return Artifact.Builder.newBuilder().id(dtoEntity.getId()).build();
    }

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"targetRepository", "buildRecords", "dependantBuildRecords","importDate",
            "distributedInProductMilestones", "identifierSha256", "built", "imported", "trusted", "descriptiveString"
    })
    ArtifactRef toRef(Artifact dbEntity);
    
    @Override
    @Mapping(target = "buildRecords", source = "buildIds")
    @Mapping(target = "dependantBuildRecords", source = "dependantBuildIds")
    /* Builder that MapStruct uses when generating mapper has method dependantBuildRecord() which confuses MapStruct as he thinks it is a new property */
    @Mapping(target = "dependantBuildRecord", ignore = true)
    /* Same as above */
    @Mapping(target = "buildRecord", ignore = true)
    @Mapping(target = "distributedInProductMilestones", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"deployUrl", "publicUrl"})
    Artifact toEntity(org.jboss.pnc.dto.Artifact dtoEntity);

    public static class IDMapper {

        public Integer toId(Artifact artifact) {
            return artifact.getId();
        }

        public Artifact toId(Integer artifactId) {
            return Artifact.Builder.newBuilder().id(artifactId).build();
        }
    }
}
