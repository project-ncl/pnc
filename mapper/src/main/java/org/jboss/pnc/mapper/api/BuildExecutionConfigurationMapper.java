/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.internal.BuildExecutionConfigurationRest;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.DefaultBuildExecutionConfiguration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { ArtifactRepositoryMapper.class })
public interface BuildExecutionConfigurationMapper
        extends SimpleMapper<BuildExecutionConfigurationRest, BuildExecutionConfiguration> {

    @Override
    @Mapping(target = "userId", source = "user")
    @BeanMapping(resultType = DefaultBuildExecutionConfiguration.class)
    BuildExecutionConfiguration toEntity(BuildExecutionConfigurationRest buildExecutionConfigurationRest);

    @Override
    @Mapping(target = "user", source = "userId")
    BuildExecutionConfigurationRest toDTO(BuildExecutionConfiguration entity);

    static User toUser(String id) {
        if (id == null) {
            return null;
        }
        return User.builder().id(id).build();
    }

    static String fromUser(User user) {
        if (user == null) {
            return null;
        }
        return user.getId();
    }
}
