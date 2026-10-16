/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.BuildResultRest;
import org.jboss.pnc.mapper.OptionalMapper;
import org.jboss.pnc.mapper.ProcessExceptionMapper;
import org.jboss.pnc.spi.BuildResult;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */

@Mapper(
        config = MapperCentralConfig.class,
        uses = {
                BuildExecutionConfigurationMapper.class,
                EnvironmentDriverResultMapper.class,
                BuildDriverResultMapper.class,
                RepourResultMapper.class,
                RepositoryManagerResultMapper.class,
                OptionalMapper.class,
                ProcessExceptionMapper.class,
                AttachmentMapper.class })
public interface BuildResultMapper extends SimpleMapper<BuildResultRest, BuildResult> {
    @Override
    BuildResult toEntity(BuildResultRest buildResultRest);

    @Override
    BuildResultRest toDTO(BuildResult entity);
}
