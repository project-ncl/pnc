/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.dto.DTOEntity;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.rest.configuration.SwaggerConstants;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class AbstractEndpoint<DTO extends REF, REF extends DTOEntity> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Provider<?, DTO, REF> provider;

    private final Class<DTO> dtoClass;


    public AbstractEndpoint(Provider<?, DTO, REF> provider, Class<DTO> dtoClass) {
        this.provider = provider;
        this.dtoClass = dtoClass;
    }

    protected DTO create(DTO dto) {
        logger.debug("Creating an entity with body: " + dto.toString());
        try{
            dto = provider.store(dto);
        } catch (ConflictedEntryException e) {
            logger.debug("There was a conflict while creating entity: " + dto);
            throw new WebApplicationException("There was a conflict while creating entity: " + dto, e, Response.status(Response.Status.CONFLICT).build());
        } catch (DTOValidationException e) {
            logger.debug("Validation error while creating entity: " + dtoClass.getSimpleName() + " with body: " + dto.toString());
            throw new BadRequestException("Validation error while creating entity: " + dtoClass.getSimpleName() + " with body: " + dto.toString(), e);
        }
        logger.debug("Entity with id: " + dto.getId() + " successfully created");
        return dto;
    }

    protected DTO getSpecific(int id) {
        logger.debug("Getting " + dtoClass.getSimpleName() + " with id: " + id);
        DTO dto = provider.getSpecific(id);
        if (dto == null) {
            logger.debug("Entity of type " + dtoClass.getSimpleName() + " with id: " + id + " not found.");
            throw new NotFoundException("Entity of type " + dtoClass.getSimpleName() + " with id: " + id + " not found.");
        }
        logger.debug("Successful retrieval of " + dtoClass.getSimpleName() + " with id: " + id);
        return dto;
    }

    protected Page<DTO> getAll(@NotNull PageParameters pageParameters) {
        logger.debug("Retrieving " + dtoClass.getSimpleName() + "s with these " + pageParameters.toString());
        return provider.getAll(pageParameters.getPageIndex(), pageParameters.getPageSize(), pageParameters.getSort(), pageParameters.getQ());
    };

    protected void update(int id, DTO dto) {
        logger.debug("Updating " + dtoClass.getSimpleName() + " with id: " + id);
        try {
            provider.update(id, dto);
        } catch (ConflictedEntryException e) {
            logger.debug("There was a conflict while updating entity + " + dto);
            /** throws status code: {@link SwaggerConstants.CONFLICTED_CODE}*/
            throw new WebApplicationException("There was a conflict while updating entity + " + dto, e, Response.status(Response.Status.CONFLICT).build());
        } catch (DTOValidationException e) {
            logger.debug("Validation error on entity: " + dtoClass.getSimpleName() + " with body: " + dto.toString());
            /** This will throw 400 {@link SwaggerConstants.INVALID_CODE} when validation fails as it's in contracts in endpoints */
            throw new BadRequestException("Validation error on entity: " + dtoClass.getSimpleName() + " with body: " + dto.toString(), e);
        }
        /** should give 204 status code {@link javax.ws.rs.core.Response.Status.NO_CONTENT} */
        return;
    }

    protected void delete(int id) {
        logger.debug("Deleting " + dtoClass.getSimpleName() + " with id: " + id);
        try {
            provider.delete(id);
        } catch (DTOValidationException e) {
            logger.debug("Entity of type " + dtoClass.getSimpleName() + "with id " + id + " wasn't found");
            /** see {@link #update(int, DTOEntity)} */
            throw new BadRequestException("Entity of type " + dtoClass.getSimpleName() + "with id " + id + " wasn't found", e);
        }
        //throw new WebApplicationException("Entity of type " + dtoClass.getSimpleName() + " with id " + id + " deleted",Response.noContent().build());
        logger.debug("Deletion of " + dtoClass.getSimpleName() + " with id: " + id + " was successfull");
        /** see {@link #update(int, DTOEntity)} */
        return;
    }
}
