/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.pnc.remotecoordinator.rexclient.exception.BadRequestException;
import org.jboss.pnc.rex.dto.responses.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BadRequestMapper implements ResponseExceptionMapper<BadRequestException> {

    @Override
    public BadRequestException toThrowable(Response response) {
        var error = response.readEntity(ErrorResponse.class);
        var detailedMessage = (String) error.object;
        log.error(
                "Client returned Bad Request. Exception: {}, Message: {}, Additional info: {}",
                error.errorType,
                error.errorMessage,
                detailedMessage);
        return new BadRequestException(error.errorMessage, detailedMessage);
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == BAD_REQUEST.getStatusCode();
    }
}
