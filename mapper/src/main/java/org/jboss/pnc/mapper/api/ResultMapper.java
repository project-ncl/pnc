/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.response.DeleteOperationResult;
import org.jboss.pnc.spi.coordinator.Result;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(config = MapperCentralConfig.class)
public interface ResultMapper {

    Result toEntity(DeleteOperationResult dtoEntity);

    @BeanMapping(ignoreUnmappedSourceProperties = "success")
    DeleteOperationResult toDTO(Result entity);
}
