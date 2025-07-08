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

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.ProductMilestone;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { RefToReferenceMapper.class, ProductVersionMapper.class, ProductReleaseMapper.class, UserMapper.class })
public interface ProductMilestoneMapper extends
        UpdatableEntityMapper<Integer, ProductMilestone, org.jboss.pnc.dto.ProductMilestone, ProductMilestoneRef> {

    IntIdMapper idMapper = new IntIdMapper();

    @Override
    @Mapping(target = "deliveredArtifacts", ignore = true)
    @Mapping(target = "performedBuilds", ignore = true)
    @Mapping(
            target = "deliveredArtifactsImporter",
            expression = "java( userMapper.toEntity(dtoEntity.getDeliveredArtifactsImporter() == null ? dtoEntity.getDistributedArtifactsImporter() : dtoEntity.getDeliveredArtifactsImporter()) )")
    ProductMilestone toEntity(org.jboss.pnc.dto.ProductMilestone dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endDate", ignore = true) // set only when closing milestone by specific endpoint
    @Mapping(target = "productVersion", ignore = true)
    @Mapping(target = "productRelease", ignore = true) // set only on release creation
    @Mapping(target = "deliveredArtifacts", ignore = true)
    @Mapping(target = "performedBuilds", ignore = true)
    @Mapping(target = "deliveredArtifactsImporter", ignore = true)
    void updateEntity(org.jboss.pnc.dto.ProductMilestone dtoEntity, @MappingTarget ProductMilestone target);

    @Override
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "productRelease", resultType = ProductReleaseRef.class)
    @Mapping(target = "distributedArtifactsImporter", source = "deliveredArtifactsImporter")
    @BeanMapping(ignoreUnmappedSourceProperties = { "performedBuilds", "deliveredArtifacts" })
    org.jboss.pnc.dto.ProductMilestone toDTO(ProductMilestone dbEntity);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "productVersion", "productRelease", "performedBuilds",
                    "deliveredArtifacts" })
    ProductMilestoneRef toRef(ProductMilestone dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return idMapper;
    }
}
