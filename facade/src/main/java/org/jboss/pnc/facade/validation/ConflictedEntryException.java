/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import java.util.Optional;

import org.jboss.pnc.facade.validation.model.ConflictedEntryDetailsRest;
import org.jboss.pnc.model.GenericEntity;

/**
 * Exception thrown when there is a conflict with an existing entity.
 * 
 * @see ConflictedStateException
 */
public class ConflictedEntryException extends DTOValidationException {

    private final String conflictedRecordId;
    private final Class<? extends GenericEntity<?>> conflictedEntity;

    public ConflictedEntryException(
            String message,
            Class<? extends GenericEntity<?>> conflictedEntity,
            String conflictedId) {
        super(message);
        this.conflictedRecordId = conflictedId;
        this.conflictedEntity = conflictedEntity;
    }

    public String getConflictedRecordId() {
        return conflictedRecordId;
    }

    public Class<? extends GenericEntity<?>> getConflictedEntity() {
        return conflictedEntity;
    }

    @Override
    public Optional<Object> getRestModelForException() {
        return Optional.of(new ConflictedEntryDetailsRest(this));
    }
}
