/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.validation;

import org.jboss.pnc.facade.validation.model.InvalidEntityDetailsRest;

import javax.validation.ConstraintViolation;

import java.lang.reflect.Field;
import java.util.Optional;

public class InvalidEntityException extends DTOValidationException {

    private final String field;

    public InvalidEntityException(String message) {
        super(message);
        this.field = null;
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
        return validationProblem.getPropertyPath().iterator().next().getName();
    }

    public String getField() {
        return field;
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.of(new InvalidEntityDetailsRest(this));
    }

}
