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
import org.jboss.pnc.model.ProductRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { RefToReferenceMapper.class, ProductMilestoneMapper.class, ProductVersionMapper.class })
public interface ProductReleaseMapper
        extends UpdatableEntityMapper<Integer, ProductRelease, org.jboss.pnc.dto.ProductRelease, ProductReleaseRef> {
    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "productVersion" })
    ProductRelease toEntity(org.jboss.pnc.dto.ProductRelease dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productMilestone", ignore = true)
    void updateEntity(org.jboss.pnc.dto.ProductRelease dtoEntity, @MappingTarget ProductRelease target);

    @Override
    @Mapping(
            target = "productVersion",
            source = "productMilestone.productVersion",
            resultType = ProductVersionRef.class)
    @Mapping(target = "productMilestone", resultType = ProductMilestoneRef.class)
    org.jboss.pnc.dto.ProductRelease toDTO(ProductRelease dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "productMilestone", "productVersion" })
    ProductReleaseRef toRef(ProductRelease dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
