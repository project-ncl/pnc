/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedStateException;
import org.jboss.pnc.facade.validation.CorruptedDataException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ValidationExceptionExceptionMapper implements ExceptionMapper<DTOValidationException> {

    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionExceptionMapper.class);

    @Override
    public Response toResponse(DTOValidationException e) {
        Response.StatusType status;
        if (e instanceof ConflictedEntryException) {
            status = Response.Status.CONFLICT;
            logger.debug("A ConflictedEntry error occurred when processing REST call", e);
        } else if (e instanceof ConflictedStateException) {
            status = Response.Status.CONFLICT;
            logger.debug("A ConflictedStateException error occurred when processing REST call", e);
        } else if (e instanceof CorruptedDataException) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            logger.error("Inconsistent data found in the system.", e);
        } else if (e instanceof EmptyEntityException) {
            status = Response.Status.NOT_FOUND;
            logger.debug("Entity not found", e);
        } else {
            status = Response.Status.BAD_REQUEST;
            logger.warn("A validation error occurred when processing REST call", e);
        }
        return Response.status(status).entity(new ErrorResponse(e, e.getRestModelForException().orElse(null))).build();
    }
}
