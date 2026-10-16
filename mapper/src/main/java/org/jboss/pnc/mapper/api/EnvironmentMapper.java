/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.BuildEnvironment;
import org.mapstruct.Mapper;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface EnvironmentMapper extends EntityMapper<Integer, BuildEnvironment, Environment, Environment> {

    @Override
    BuildEnvironment toEntity(Environment dtoEntity);

    @Override
    @Reference
    default Environment toRef(BuildEnvironment dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    Environment toDTO(BuildEnvironment dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
