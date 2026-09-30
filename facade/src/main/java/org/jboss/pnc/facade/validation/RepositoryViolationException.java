/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.Optional;

public class RepositoryViolationException extends DTOValidationException {

    public RepositoryViolationException(String message) {
        super(message);
    }

    public RepositoryViolationException(Throwable cause) {
        super(cause);
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.empty();
    }

}
