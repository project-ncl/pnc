/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.Optional;

/**
 * Thrown when missing or invalid reference is found.
 */
public class CorruptedDataException extends DTOValidationException {
    public CorruptedDataException(String message) {
        super(message);
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.empty();
    }
}
