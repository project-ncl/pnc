/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.ProductVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ProductVersionMapper
        extends UpdatableEntityMapper<Integer, ProductVersion, org.jboss.pnc.dto.ProductVersion, ProductVersionRef> {

    @Override
    @Mapping(target = "buildConfigurationSets", source = "groupConfigs")
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    @Mapping(target = "productReleases", ignore = true)
    ProductVersion toEntity(org.jboss.pnc.dto.ProductVersion dtoEntity);

    @Override
    @InheritConfiguration
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productMilestones", ignore = true)
    @Mapping(target = "buildConfigurationSets", expression = "java( cm.updateGroupConfigs(dtoEntity, target) )")
    @Mapping(target = "buildConfigurations", expression = "java( cm.updateBuildConfigs(dtoEntity, target) )")
    void updateEntity(org.jboss.pnc.dto.ProductVersion dtoEntity, @MappingTarget ProductVersion target);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "product",
                    "productReleases",
                    "productMilestones",
                    "currentProductMilestone",
                    "buildConfigurationSets",
                    "buildConfigurations" })
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
