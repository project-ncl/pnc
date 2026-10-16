/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jboss.pnc.dto.validation.groups.ValidationGroup;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

public class ValidationBuilder<T> {

    private Class<? extends ValidationGroup> validationGroup;
    private T objectToBeValidated;

    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ValidationBuilder(Class<? extends ValidationGroup> validationGroup, T objectToBeValidated) {
        this.validationGroup = validationGroup;
        this.objectToBeValidated = objectToBeValidated;
    }

    public static <T> ValidationBuilder<T> validateObject(T object, Class<? extends ValidationGroup> validationGroup) {
        if (validationGroup == ValidationGroup.class) {
            throw new IllegalArgumentException("Use validation subclasses");
        }

        return new ValidationBuilder<>(validationGroup, object);
    }

    public static ValidationBuilder validateObject(Class<? extends ValidationGroup> validationGroup) {
        return validateObject(null, validationGroup);
    }

    public ValidationBuilder<T> validateAnnotations() throws InvalidEntityException {
        if (objectToBeValidated != null) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(objectToBeValidated, validationGroup);
            if (!constraintViolations.isEmpty()) {
                throw new InvalidEntityException(constraintViolations.iterator().next());
            }
        }
        return this;
    }

    public ValidationBuilder<T> validateField(String property, Object value) throws InvalidEntityException {
        if (objectToBeValidated != null) {
            try {
                Field field = objectToBeValidated.getClass().getDeclaredField(property);
                field.setAccessible(true);
                Object valueFromObject = field.get(objectToBeValidated);
                if ((value == null && valueFromObject == null) || (value != null && value.equals(valueFromObject)))
                    return this;
                throw new InvalidEntityException(field);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such field", e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not access field", e);
            }
        }
        return this;
    }

    public ValidationBuilder<T> validateConflict(ConflictedEntryValidator validate) throws ConflictedEntryException {
        ConflictedEntryValidator.ConflictedEntryValidationError validationError = validate.validate();
        if (validationError != null) {
            throw new ConflictedEntryException(
                    validationError.getMessage(),
                    validationError.getConflictedEntity(),
                    validationError.getConflictedRecordId().toString());
        }
        return this;
    }

    public ValidationBuilder<T> validateNotEmptyArgument() throws EmptyEntityException {
        if (objectToBeValidated == null) {
            throw new EmptyEntityException("Input object is null");
        }
        return this;
    }

    public <DBEntity extends GenericEntity<ID>, ID extends Serializable> ValidationBuilder<T> validateAgainstRepository(
            Repository<DBEntity, ID> repository,
            ID id,
            boolean shouldExist) throws RepositoryViolationException, EmptyEntityException {

        if (id == null) {
            throw new EmptyEntityException("Id is null");
        }

        DBEntity dbEntity = repository.queryById(id);
        if ((!shouldExist && dbEntity != null) || (shouldExist && dbEntity == null)) {
            StringBuilder sb = new StringBuilder("Entity should ");
            if (!shouldExist) {
                sb.append(" not ");
            }
            sb.append("exist in the DB");
            throw new RepositoryViolationException(sb.toString());
        }
        return this;
    }

    public ValidationBuilder<T> validateCondition(boolean condition, String message) throws InvalidEntityException {
        if (!condition) {
            throw new InvalidEntityException(message);
        }
        return this;
    }

}
