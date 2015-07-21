/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.rest.provider.ConflictedEntryException;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class ErrorResponseDetailsRest {

    private String conflictedRecordId;
    private String conflictedEntity;

    public ErrorResponseDetailsRest() {
    }

    public ErrorResponseDetailsRest(ConflictedEntryException conflictedEntryException) {
        this.conflictedEntity = conflictedEntryException.getConflictedEntity().getSimpleName();
        this.conflictedRecordId = conflictedEntryException.getConflictedRecordId().toString();
    }

    public String getConflictedRecordId() {
        return conflictedRecordId;
    }

    public String getConflictedEntity() {
        return conflictedEntity;
    }
}
