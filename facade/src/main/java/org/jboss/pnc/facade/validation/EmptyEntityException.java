/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.Optional;

public class EmptyEntityException extends DTOValidationException {

    public EmptyEntityException(String message) {
        super(message);
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.empty();
    }

}
