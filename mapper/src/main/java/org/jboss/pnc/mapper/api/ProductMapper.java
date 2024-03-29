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

import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(config = MapperCentralConfig.class, uses = { MapSetMapper.class })
public interface ProductMapper extends UpdatableEntityMapper<Integer, Product, org.jboss.pnc.dto.Product, ProductRef> {
    @Override
    Product toEntity(org.jboss.pnc.dto.Product dtoEntity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productVersions", ignore = true)
    public abstract void updateEntity(org.jboss.pnc.dto.Product dtoEntity, @MappingTarget Product target);

    @Override
    org.jboss.pnc.dto.Product toDTO(Product dbEntity);

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "productVersions" })
    ProductRef toRef(Product dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
