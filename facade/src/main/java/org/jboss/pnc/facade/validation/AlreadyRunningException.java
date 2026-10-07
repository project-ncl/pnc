/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AlreadyRunningException extends RuntimeException {

    private Object responseObject;

    public AlreadyRunningException(String message, Object o) {
        super(message);
        this.responseObject = o;
    }

    public AlreadyRunningException(String message, Throwable cause, Object o) {
        super(message, cause);
        this.responseObject = o;
    }

    public AlreadyRunningException(Throwable cause, Object o) {
        super(cause);
        this.responseObject = o;
    }

    public AlreadyRunningException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace,
            Object o) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseObject = o;
    }

    public AlreadyRunningException(Object o) {
        super();
        this.responseObject = o;
    }

    public Object getResponseObject() {
        return responseObject;
    }
}
