/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.provider;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.pnc.remotecoordinator.rexclient.exception.TaskNotFoundException;
import org.jboss.pnc.rex.dto.responses.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotFoundMapper implements ResponseExceptionMapper<NotFoundException> {

    @Override
    public NotFoundException toThrowable(Response response) {
        try {
            var error = response.readEntity(ErrorResponse.class);
            log.error("Resource not found. Rex Exception: {}, Message: {}", error.errorType, error.errorMessage);
            return new TaskNotFoundException(error.errorMessage);
        } catch (Exception e) {
            // When the response is unexpected 404 (eg. wrong path), the response body cannot be read to ErrorResponse.
            String message = "Cannot read response body.";
            log.error(message, e);
            return new NotFoundException(message);
        }
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == NOT_FOUND.getStatusCode();
    }
}
