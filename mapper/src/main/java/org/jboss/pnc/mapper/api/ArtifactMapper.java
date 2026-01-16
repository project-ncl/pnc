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

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.Artifact;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ArtifactMapper
        extends UpdatableEntityMapper<Integer, Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef> {

    IntIdMapper idMapper = new IntIdMapper();

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @Mapping(target = "build", source = "buildRecord")
    @Mapping(target = "targetRepository", qualifiedBy = Reference.class)
    @Mapping(target = "creationUser", qualifiedBy = Reference.class)
    @Mapping(target = "modificationUser", qualifiedBy = Reference.class)
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "deliveredInProductMilestones", "identifierSha256", "built", "imported",
                    "trusted", "descriptiveString", "dependantBuildRecords" })
    org.jboss.pnc.dto.Artifact toDTO(Artifact dbEntity);

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "targetRepository", "dependantBuildRecords", "importDate",
                    "deliveredInProductMilestones", "identifierSha256", "built", "imported", "trusted",
                    "descriptiveString", "creationUser", "modificationUser" })
    ArtifactRef toRef(Artifact dbEntity);

    @Override
    @Mapping(target = "creationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "modificationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildRecord", source = "build")
    @Mapping(target = "dependantBuildRecords", ignore = true)
    @Mapping(target = "targetRepository", qualifiedBy = IdEntity.class)
    /*
     * Builder that MapStruct uses when generating mapper has method dependantBuildRecord() which confuses MapStruct as
     * he thinks it is a new property
     */
    @Mapping(target = "dependantBuildRecord", ignore = true)
    @Mapping(target = "deliveredInProductMilestones", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "deployUrl", "publicUrl" })
    Artifact toEntity(org.jboss.pnc.dto.Artifact dtoEntity);

    /**
     * Created model.Artifact can contain target repository entity in transient (in case id is null) or unspecified (in
     * case id is not null) JPA state.
     */
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "targetRepository")
    @TransientTargetRepo
    Artifact toEntityWithTransientTargetRepository(org.jboss.pnc.dto.Artifact dtoEntity);

    @Override
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationUser", ignore = true) // will be set when updating
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true) // will be set when updating
    @BeanMapping(ignoreUnmappedSourceProperties = { "deployUrl", "publicUrl" })
    void updateEntity(org.jboss.pnc.dto.Artifact dtoEntity, @MappingTarget org.jboss.pnc.model.Artifact target);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
