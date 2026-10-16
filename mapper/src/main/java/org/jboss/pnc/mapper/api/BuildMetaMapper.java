/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.BuildMeta;
import org.mapstruct.Mapper;

@Mapper(config = MapperCentralConfig.class)
public interface BuildMetaMapper extends SimpleMapper<BuildMeta, org.jboss.pnc.spi.coordinator.BuildMeta> {

    @Override
    org.jboss.pnc.spi.coordinator.BuildMeta toEntity(BuildMeta buildMeta);

    @Override
    BuildMeta toDTO(org.jboss.pnc.spi.coordinator.BuildMeta entity);
}
