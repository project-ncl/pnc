/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface GroupConfigurationMapper
        extends UpdatableEntityMapper<Integer, BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> {

    @Override
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "buildConfigSetRecords", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "buildConfigurations", source = "buildConfigs")
    BuildConfigurationSet toEntity(GroupConfiguration dtoEntity);

    @Override
    @InheritConfiguration
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true) // archival done by special endpoint
    @Mapping(target = "buildConfigurations", expression = "java( cm.updateBuildConfigs(dtoEntity, target) )")
    public abstract void updateEntity(GroupConfiguration dtoEntity, @MappingTarget BuildConfigurationSet target);

    @Override
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "productVersion",
                    "buildConfigurations",
                    "buildConfigSetRecords",
                    "archived",
                    "active",
                    "currentProductMilestone" })
    GroupConfigurationRef toRef(BuildConfigurationSet dbEntity);

    @Override
    @Mapping(target = "productVersion", resultType = ProductVersionRef.class)
    @Mapping(target = "buildConfigs", source = "buildConfigurations")
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "buildConfigSetRecords",
                    "active",
                    "currentProductMilestone",
                    "archived" })
    GroupConfiguration toDTO(BuildConfigurationSet dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
