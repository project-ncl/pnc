/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper which maps {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry} to
 * {@link org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry}.
 */
@Mapper(config = MapperCentralConfig.class, uses = { UserMapper.class })
public interface DeliverableAnalyzerLabelEntryMapper {

    @Mapping(target = "date", source = "entryTime")
    @BeanMapping(ignoreUnmappedSourceProperties = { "id" })
    DeliverableAnalyzerLabelEntry toDto(org.jboss.pnc.model.DeliverableAnalyzerLabelEntry entity);
}
