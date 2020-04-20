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

import org.jboss.pnc.dto.BuildPushResultRef;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneCloseResultRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.mapper.UUIDMapper;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { UUIDMapper.class, ProductMilestoneMapper.class, BuildPushResultMapper.class })
public interface ProductMilestoneCloseResultMapper extends
        EntityMapper<UUID, ProductMilestoneRelease, ProductMilestoneCloseResult, ProductMilestoneCloseResultRef> {

    @Override
    @Mapping(target = "log", ignore = true)
    @Mapping(target = "buildRecordPushResults", source = "buildPushResults")
    ProductMilestoneRelease toEntity(ProductMilestoneCloseResult dtoEntity);

    @Override
    default ProductMilestoneRelease toIDEntity(ProductMilestoneCloseResultRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        ProductMilestoneRelease milestoneRelease = new ProductMilestoneRelease();
        milestoneRelease.setId(UUID.fromString(dtoEntity.getId()));
        return milestoneRelease;
    }

    @Override
    @Mapping(target = "milestone", resultType = ProductMilestoneRef.class)
    @Mapping(target = "buildPushResults", source = "buildRecordPushResults", resultType = BuildPushResultRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "log" })
    ProductMilestoneCloseResult toDTO(ProductMilestoneRelease dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "milestone", "log", "buildRecordPushResults" })
    ProductMilestoneCloseResultRef toRef(ProductMilestoneRelease dbEntity);
}
