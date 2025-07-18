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

import org.jboss.pnc.facade.validation.model.ConflictedEntryDetailsRest;
import org.jboss.pnc.model.GenericEntity;

import java.util.Optional;

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
