/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.pnc.dto.response.ErrorResponse;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;

@Provider
public class InvalidLabelOperationExceptionMapper implements ExceptionMapper<InvalidLabelOperationException> {

    @Override
    public Response toResponse(InvalidLabelOperationException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("InvalidLabelOperationException", e.getMessage()))
                .build();
    }
}
