/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.AlreadyRunningException;

@Provider
public class AlreadyRunningExceptionsMapper implements ExceptionMapper<AlreadyRunningException> {

    @Override
    public Response toResponse(AlreadyRunningException e) {
        Response.StatusType status = Response.Status.CONFLICT;
        return Response.status(status).entity(new ErrorResponse(e, e.getResponseObject())).build();
    }
}
