/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.rsql.RSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RSQLExceptionMapper implements ExceptionMapper<RSQLException> {

    private static final Logger logger = LoggerFactory.getLogger(RSQLExceptionMapper.class);

    @Override
    public Response toResponse(RSQLException e) {
        Response.StatusType status = Response.Status.BAD_REQUEST;
        logger.debug("An RSQL error occurred when processing REST call", e);
        return Response.status(status).entity(new ErrorResponse(e)).build();
    }
}
