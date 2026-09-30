/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import java.io.Serializable;

import javax.ws.rs.NotFoundException;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointHelper<DBEntityID extends Serializable, DTO extends REF, REF extends DTOEntity> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<DTO> dtoClass;
    private final Provider<DBEntityID, ?, DTO, REF> provider;

    protected EndpointHelper(Class<DTO> dtoClass, Provider<DBEntityID, ?, DTO, REF> provider) {
        this.dtoClass = dtoClass;
        this.provider = provider;
    }

    protected DTO create(DTO dto) {
        logger.debug("Creating an entity with body: {}", dto);
        dto = provider.store(dto);
        logger.debug("Entity with id: {} successfully created", dto.getId());
        return dto;
    }

    protected DTO getSpecific(String id) {
        logger.debug("Getting {} with id: {}", dtoClass.getSimpleName(), id);
        DTO dto = provider.getSpecific(id);
        if (dto == null) {
            logger.debug("Entity of type {} with id: {} not found.", dtoClass.getSimpleName(), id);
            throw new NotFoundException(
                    "Entity of type " + dtoClass.getSimpleName() + " with id: " + id + " not found.");
        }
        logger.debug("Successful retrieval of {} with id: {}", dtoClass.getSimpleName(), id);
        return dto;
    }

    protected Page<DTO> getAll(PageParameters pageParameters) {
        logger.debug("Retrieving {}s with these {}", dtoClass.getSimpleName(), pageParameters);
        return provider.getAll(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ());
    }

    protected DTO update(String id, DTO dto) {
        logger.debug("Updating {} with id: {}", dtoClass.getSimpleName(), id);
        return provider.update(id, dto);
    }

    protected void delete(String id) {
        logger.debug("Deleting {} with id: {}", dtoClass.getSimpleName(), id);
        provider.delete(id);
        logger.debug("Deletion of {} with id: {} was successful", dtoClass.getSimpleName(), id);
    }
}
