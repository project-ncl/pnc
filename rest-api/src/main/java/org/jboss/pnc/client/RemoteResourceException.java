/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.Optional;

import javax.ws.rs.WebApplicationException;

import org.jboss.pnc.dto.response.ErrorResponse;

import lombok.Getter;

/**
 * Client exception, which indicates a failure of the request to the server.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
public class RemoteResourceException extends ClientException {

    @Getter
    private final int status;

    private final ErrorResponse response;

    public RemoteResourceException(Throwable cause) {
        super(cause);
        this.status = -1;
        this.response = null;
    }

    public RemoteResourceException(WebApplicationException cause) {
        super(cause);
        this.status = cause.getResponse().getStatus();
        this.response = null;
    }

    public RemoteResourceException(ErrorResponse response, WebApplicationException cause) {
        super(response == null ? cause.getMessage() : response.getErrorMessage(), cause);
        this.status = cause.getResponse().getStatus();
        this.response = response;
    }

    public RemoteResourceException(String message, int status) {
        super(message + " status: " + status);
        this.status = status;
        this.response = null;
    }

    public Optional<ErrorResponse> getResponse() {
        return Optional.ofNullable(response);
    }

}
