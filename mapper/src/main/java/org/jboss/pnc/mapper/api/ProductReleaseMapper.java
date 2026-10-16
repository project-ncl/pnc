/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
