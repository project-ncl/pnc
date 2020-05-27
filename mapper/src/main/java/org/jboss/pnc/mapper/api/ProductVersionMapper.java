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
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.model.ProductVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { ProductMilestoneMapper.class, ProductMapper.class, MapSetMapper.class })
public interface ProductVersionMapper
        extends EntityMapper<Integer, ProductVersion, org.jboss.pnc.dto.ProductVersion, ProductVersionRef> {

    @Override
    default ProductVersion toIDEntity(ProductVersionRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        ProductVersion entity = new ProductVersion();
        entity.setId(Integer.valueOf(dtoEntity.getId()));
        return entity;
    }

    @Override
    @Mapping(target = "buildConfigurationSets", source = "groupConfigs")
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    @Mapping(target = "productReleases", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "productReleases" })
    ProductVersion toEntity(org.jboss.pnc.dto.ProductVersion dtoEntity);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "product", "productReleases", "productMilestones",
                    "currentProductMilestone", "buildConfigurationSets", "buildConfigurations" })
    ProductVersionRef toRef(ProductVersion dbEntity);

    @Override
    @Mapping(target = "groupConfigs", source = "buildConfigurationSets")
    @Mapping(target = "product", resultType = ProductRef.class)
    @Mapping(target = "currentProductMilestone", resultType = ProductMilestoneRef.class)
    @Mapping(target = "buildConfigs", source = "buildConfigurations")
    org.jboss.pnc.dto.ProductVersion toDTO(ProductVersion dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
