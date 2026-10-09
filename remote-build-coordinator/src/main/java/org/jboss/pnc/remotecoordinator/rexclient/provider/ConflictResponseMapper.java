/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.provider;

import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.pnc.remotecoordinator.rexclient.exception.BCAConflictException;
import org.jboss.pnc.remotecoordinator.rexclient.exception.BuildIDConflictException;
import org.jboss.pnc.remotecoordinator.rexclient.exception.ConflictResponseException;
import org.jboss.pnc.rex.dto.responses.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConflictResponseMapper implements ResponseExceptionMapper<ConflictResponseException> {

    @Override
    public ConflictResponseException toThrowable(Response response) {
        var error = response.readEntity(ErrorResponse.class);
        switch (error.errorType) {
            case "TaskConflictException":
                String buildId = (String) error.object;
                return new BuildIDConflictException(error.errorMessage, buildId);
            case "ConstraintConflictException":
                String bcaIdRev = (String) error.object;
                return new BCAConflictException(error.errorMessage, bcaIdRev);
            default:
                return new ConflictResponseException("409 Conflict with unknown errorType");
        }
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == CONFLICT.getStatusCode();
    }
}
