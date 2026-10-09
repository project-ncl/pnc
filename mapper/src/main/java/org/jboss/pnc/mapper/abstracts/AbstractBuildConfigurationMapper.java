/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.abstracts;

import javax.inject.Inject;

import org.jboss.pnc.mapper.CollectionMerger;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.mapper.api.MapperCentralConfig;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.mapper.api.ProjectMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.mapstruct.Mapper;

/**
 *
 * @author jbrazdil
 */
@Mapper(
        config = MapperCentralConfig.class,
        implementationName = "BuildConfigurationMapperImpl",
        uses = {
                RefToReferenceMapper.class,
                ProjectMapper.class,
                ProductVersionMapper.class,
                EnvironmentMapper.class,
                BuildConfigurationMapper.IDMapper.class,
                SCMRepositoryMapper.class,
                MapSetMapper.class,
                UserMapper.class })
public abstract class AbstractBuildConfigurationMapper implements BuildConfigurationMapper {

    protected CollectionMerger cm;

    @Inject
    public void setCm(CollectionMerger cm) {
        this.cm = cm;
    }
}
