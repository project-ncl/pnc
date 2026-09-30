/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.ejb.EJBAccessException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<EJBAccessException> {
    private static final Logger log = LoggerFactory.getLogger(UnauthorizedExceptionMapper.class);

    @Override
    public Response toResponse(EJBAccessException exception) {
        log.info("A user is trying to access restricted resource", exception);

        String errorMessage = "Insufficient privileges: the required role to access the resource is missing in the provided JWT.";
        Response.ResponseBuilder builder = Response.status(FORBIDDEN);
        builder.entity(
                ErrorResponse.builder()
                        .errorType(exception.getClass().getSimpleName())
                        .errorMessage(errorMessage)
                        .details(exception.getMessage())
                        .build())
                .type(MediaType.APPLICATION_JSON);

        return builder.build();
    }
}
