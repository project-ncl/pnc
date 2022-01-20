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
package org.jboss.pnc.facade.validation.model;

import org.jboss.pnc.facade.validation.ConflictedEntryException;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class ConflictedEntryDetailsRest {

    private String conflictedRecordId;
    private String conflictedEntity;
    private ConflictedEntryException conflictedEntryException;

    public ConflictedEntryDetailsRest() {
    }

    public ConflictedEntryDetailsRest(ConflictedEntryException conflictedEntryException) {
        this.conflictedEntryException = conflictedEntryException;
        this.conflictedEntity = conflictedEntryException.getConflictedEntity().getSimpleName();
        this.conflictedRecordId = conflictedEntryException.getConflictedRecordId();
    }

    public String getConflictedRecordId() {
        return conflictedRecordId;
    }

    public String getConflictedEntity() {
        return conflictedEntity;
    }
}
