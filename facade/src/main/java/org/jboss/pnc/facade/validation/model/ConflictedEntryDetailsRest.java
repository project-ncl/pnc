/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation.model;

import javax.xml.bind.annotation.XmlType;

import org.jboss.pnc.facade.validation.ConflictedEntryException;

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
