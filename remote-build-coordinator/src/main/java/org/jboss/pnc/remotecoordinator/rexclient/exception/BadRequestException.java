/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.exception;

/**
 * HTTP Request returned 404 Bad Request
 */
public class BadRequestException extends RuntimeException {

    private final String detail;

    public BadRequestException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public BadRequestException(String message, Throwable cause, String detail) {
        super(message, cause);
        this.detail = detail;
    }

    public BadRequestException(Throwable cause, String detail) {
        super(cause);
        this.detail = detail;
    }
}
