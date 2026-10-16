/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.BuildDriverResultRest;
import org.jboss.pnc.mapper.OptionalMapper;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.DefaultBuildDriverResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { OptionalMapper.class })
public interface BuildDriverResultMapper extends SimpleMapper<BuildDriverResultRest, BuildDriverResult> {

    @Override
    @BeanMapping(resultType = DefaultBuildDriverResult.class)
    BuildDriverResult toEntity(BuildDriverResultRest buildDriverResultRest);

    @Override
    BuildDriverResultRest toDTO(BuildDriverResult entity);
}
