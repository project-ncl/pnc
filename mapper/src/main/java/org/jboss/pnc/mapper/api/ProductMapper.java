/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
