/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class BuildConflictExceptionMapper implements ExceptionMapper<BuildConflictException> {

    private static final Logger logger = LoggerFactory.getLogger(BuildConflictExceptionMapper.class);

    @Override
    public Response toResponse(BuildConflictException e) {
        Response.Status status = Response.Status.CONFLICT;
        logger.warn("A BuildConflict error occurred when processing REST call", e);
        return Response.status(status)
                .entity(new ErrorResponse("BuildConflictException", e.getMessage() + ": " + e.getBuildTaskId()))
                .build();
    }
}
