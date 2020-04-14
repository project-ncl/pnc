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

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { ProductVersionMapper.class, GroupConfigurationMapper.class, BuildMapper.IDMapper.class,
                UserMapper.class })
public interface GroupBuildMapper extends EntityMapper<Integer, BuildConfigSetRecord, GroupBuild, GroupBuildRef> {

    @Override
    @Mapping(target = "buildConfigurationSet", source = "groupConfig")
    @Mapping(target = "buildRecords", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "user", qualifiedBy = IdEntity.class)
    BuildConfigSetRecord toEntity(GroupBuild dtoEntity);

    @Override
    default BuildConfigSetRecord toIDEntity(GroupBuildRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        BuildConfigSetRecord bcsr = new BuildConfigSetRecord();
        bcsr.setId(Integer.valueOf(dtoEntity.getId()));
        return bcsr;
    }

    @Override
    @Mapping(target = "groupConfig", source = "buildConfigurationSet", resultType = GroupConfigurationRef.class)
    @Mapping(target = "user", qualifiedBy = Reference.class)
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "attributes", "buildRecords" })
    GroupBuild toDTO(BuildConfigSetRecord dbEntity);

    @Override
    // Workaround for NCL-4228
    @Reference
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "attributes", "buildRecords", "buildConfigurationSet", "user",
                    "productVersion" })
    GroupBuildRef toRef(BuildConfigSetRecord dbEntity);
}
