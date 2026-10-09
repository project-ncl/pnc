/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.util;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HttpResponseException extends RuntimeException {

    private String status;

    HttpResponseException(String status, String message, Throwable cause) {
        super(message != null ? message : "HTTP error - " + status, cause);
        this.status = status;
    }

    public int getStatusCode() {
        return Integer.valueOf(status.split(" ")[0]);
    }
}
