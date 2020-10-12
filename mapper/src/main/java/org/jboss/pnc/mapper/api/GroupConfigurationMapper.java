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

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { RefToReferenceMapper.class, ProductVersionMapper.class, MapSetMapper.class })
public interface GroupConfigurationMapper
        extends EntityMapper<Integer, BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> {

    @Override
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "buildConfigSetRecords", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    BuildConfigurationSet toEntity(GroupConfiguration dtoEntity);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "productVersion", "buildConfigurations", "buildConfigSetRecords",
                    "archived", "active", "currentProductMilestone" })
    GroupConfigurationRef toRef(BuildConfigurationSet dbEntity);

    @Override
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "buildConfigs", source = "buildConfigurations")
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "buildConfigSetRecords", "active", "currentProductMilestone",
                    "archived" })
    GroupConfiguration toDTO(BuildConfigurationSet dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
