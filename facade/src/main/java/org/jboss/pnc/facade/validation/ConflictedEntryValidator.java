/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation;

import org.jboss.pnc.model.GenericEntity;

/**
 * Conflict validation class
 *
 * @author Sebastian Laskawiec
 */
@FunctionalInterface
public interface ConflictedEntryValidator {

    class ConflictedEntryValidationError<ID> {
        private final ID conflictedRecordId;
        private final Class<? extends GenericEntity<?>> conflictedEntity;
        private final String message;

        public ConflictedEntryValidationError(
                ID conflictedRecordId,
                Class<? extends GenericEntity<?>> conflictedEntity,
                String message) {
            this.conflictedRecordId = conflictedRecordId;
            this.conflictedEntity = conflictedEntity;
            this.message = message;
        }

        public ID getConflictedRecordId() {
            return conflictedRecordId;
        }

        public Class<? extends GenericEntity<?>> getConflictedEntity() {
            return conflictedEntity;
        }

        public String getMessage() {
            return message;
        }
    }

    ConflictedEntryValidationError validate();
}
