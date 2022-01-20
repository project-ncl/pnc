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

import org.jboss.pnc.dto.BuildPushResultRef;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneCloseResultRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.mapper.LongIdMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { RefToReferenceMapper.class, ProductMilestoneMapper.class, BuildPushResultMapper.class })
public interface ProductMilestoneCloseResultMapper extends
        EntityMapper<Long, ProductMilestoneRelease, ProductMilestoneCloseResult, ProductMilestoneCloseResultRef> {

    @Override
    @Mapping(target = "log", ignore = true)
    @Mapping(target = "buildRecordPushResults", source = "buildPushResults")
    ProductMilestoneRelease toEntity(ProductMilestoneCloseResult dtoEntity);

    @Override
    @Mapping(target = "milestone", resultType = ProductMilestoneRef.class)
    @Mapping(target = "buildPushResults", source = "buildRecordPushResults", resultType = BuildPushResultRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "log" })
    ProductMilestoneCloseResult toDTO(ProductMilestoneRelease dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "milestone", "log", "buildRecordPushResults" })
    ProductMilestoneCloseResultRef toRef(ProductMilestoneRelease dbEntity);

    @Override
    default IdMapper<Long, String> getIdMapper() {
        return new LongIdMapper();
    }
}
