/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.abstracts;

import javax.inject.Inject;

import org.jboss.pnc.mapper.CollectionMerger;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.MapperCentralConfig;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.mapstruct.Mapper;

/**
 *
 * @author jbrazdil
 */
@Mapper(
        config = MapperCentralConfig.class,
        implementationName = "GroupConfigurationMapperImpl",
        uses = { RefToReferenceMapper.class, ProductVersionMapper.class, MapSetMapper.class })
public abstract class AbstractGroupConfigurationMapper implements GroupConfigurationMapper {

    protected CollectionMerger cm;

    @Inject
    public void setCm(CollectionMerger cm) {
        this.cm = cm;
    }
}
