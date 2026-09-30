/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.Optional;

import javax.ejb.ApplicationException;

/**
 * TopMost Validation Exception
 *
 * @author Sebastian Laskawiec
 */
@ApplicationException(rollback = true)
public abstract class DTOValidationException extends RuntimeException {

    public DTOValidationException() {
    }

    public DTOValidationException(String message) {
        super(message);
    }

    public DTOValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DTOValidationException(Throwable cause) {
        super(cause);
    }

    public DTOValidationException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public abstract Optional<Object> getRestModelForException();
}
