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
import org.jboss.pnc.dto.ProductMilestoneReleaseRef;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Mapper(config = MapperCentralConfig.class, uses = { ProductMilestoneMapper.class })
public interface ProductMilestoneReleaseMapper extends
        EntityMapper<Integer, ProductMilestoneRelease, org.jboss.pnc.dto.ProductMilestoneRelease, ProductMilestoneReleaseRef> {

    @Override
    @Mapping(target = "log", ignore = true)
    ProductMilestoneRelease toEntity(org.jboss.pnc.dto.ProductMilestoneRelease dtoEntity);

    @Override
    default ProductMilestoneRelease toIDEntity(ProductMilestoneReleaseRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        ProductMilestoneRelease milestoneRelease = new ProductMilestoneRelease();
        milestoneRelease.setId(Integer.valueOf(dtoEntity.getId()));
        return milestoneRelease;
    }

    @Override
    @Mapping(target = "milestone", resultType = ProductMilestoneRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "log" })
    org.jboss.pnc.dto.ProductMilestoneRelease toDTO(ProductMilestoneRelease dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "milestone", "log" })
    ProductMilestoneReleaseRef toRef(ProductMilestoneRelease dbEntity);
}
