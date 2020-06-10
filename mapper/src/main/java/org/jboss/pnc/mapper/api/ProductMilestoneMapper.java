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

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.AbstractArtifactMapper;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.ProductMilestone;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { ProductVersionMapper.class, ProductReleaseMapper.class, BuildMapper.IDMapper.class,
                AbstractArtifactMapper.IDMapper.class })
public interface ProductMilestoneMapper
        extends EntityMapper<Integer, ProductMilestone, org.jboss.pnc.dto.ProductMilestone, ProductMilestoneRef> {

    @Override
    @Mapping(target = "distributedArtifacts", ignore = true)
    @Mapping(target = "performedBuilds", ignore = true)
    @Mapping(target = "issueTrackerUrl", ignore = true)
    @Mapping(target = "downloadUrl", ignore = true)
    ProductMilestone toEntity(org.jboss.pnc.dto.ProductMilestone dtoEntity);

    @Override
    default ProductMilestone toIDEntity(ProductMilestoneRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        ProductMilestone milestone = new ProductMilestone();
        milestone.setId(Integer.valueOf(dtoEntity.getId()));
        return milestone;
    }

    @Override
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "productRelease", resultType = ProductReleaseRef.class)
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "performedBuilds", "distributedArtifacts", "issueTrackerUrl",
                    "downloadUrl" })
    org.jboss.pnc.dto.ProductMilestone toDTO(ProductMilestone dbEntity);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "productVersion", "productRelease", "performedBuilds",
                    "distributedArtifacts", "issueTrackerUrl", "downloadUrl" })
    ProductMilestoneRef toRef(ProductMilestone dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
