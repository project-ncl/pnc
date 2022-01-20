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
