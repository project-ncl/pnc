/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.BuildTriggeringNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class BuildTriggeringExceptionMapper implements ExceptionMapper<BuildTriggeringNotAllowedException> {

    private static final Logger logger = LoggerFactory.getLogger(BuildTriggeringExceptionMapper.class);

    @Override
    public Response toResponse(BuildTriggeringNotAllowedException e) {
        Response.Status status = Response.Status.CONFLICT;
        logger.warn("A BuildTriggering error occurred when processing REST call", e);
        return Response.status(status).entity(new ErrorResponse("BuildTriggeringException", e.getMessage(), e)).build();
    }
}
