/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.EnvironmentDriverResultRest;
import org.jboss.pnc.mapper.OptionalMapper;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { SshCredentialsMapper.class, OptionalMapper.class })
public interface EnvironmentDriverResultMapper
        extends SimpleMapper<EnvironmentDriverResultRest, EnvironmentDriverResult> {

    @Override
    EnvironmentDriverResult toEntity(EnvironmentDriverResultRest environmentDriverResultRest);

    @Override
    EnvironmentDriverResultRest toDTO(EnvironmentDriverResult entity);
}
