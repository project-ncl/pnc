/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.spi.exception.ScheduleConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ScheduleConflictExceptionMapper implements ExceptionMapper<ScheduleConflictException> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleConflictExceptionMapper.class);

    @Override
    public Response toResponse(ScheduleConflictException e) {
        logger.warn("A ScheduleConflict error occurred when scheduling Build. {}", e.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("ScheduleConflictException", e.getMessage()))
                .build();
    }
}
