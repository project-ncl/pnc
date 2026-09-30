/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class InvalidRequestExceptionMapper implements ExceptionMapper<InvalidRequestException> {

    private static final Logger logger = LoggerFactory.getLogger(InvalidRequestExceptionMapper.class);

    @Override
    public Response toResponse(InvalidRequestException e) {
        logger.warn("A BuildRequest error occurred when processing REST call", e);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("BuildRequestException", e.getMessage()))
                .build();
    }
}
