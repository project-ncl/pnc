/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.lang.reflect.Field;
import java.util.Optional;

import javax.validation.ConstraintViolation;

import org.jboss.pnc.facade.validation.model.InvalidEntityDetailsRest;

public class InvalidEntityException extends DTOValidationException {

    private final String field;

    public InvalidEntityException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidEntityException(String message, String field) {
        super(message);
        this.field = field;
    }

    public InvalidEntityException(String message, String field, Throwable cause) {
        super(message, cause);
        this.field = field;
    }

    public InvalidEntityException(Field field) {
        super("Field validation error occurred. Field: " + field.getName());
        this.field = field.getName();
    }

    public InvalidEntityException(ConstraintViolation<?> validationProblem) {
        super("Field validation error occurred. " + getErrorDescription(validationProblem));
        this.field = getFieldName(validationProblem);
    }

    private static String getErrorDescription(ConstraintViolation<?> validationProblem) {
        String field = getFieldName(validationProblem);
        String message = validationProblem.getMessage();
        return "Field: " + field + ", problem: " + message;
    }

    private static String getFieldName(ConstraintViolation<?> validationProblem) {
        return validationProblem.getPropertyPath().toString();
    }

    public String getField() {
        return field;
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.of(new InvalidEntityDetailsRest(this));
    }

}
