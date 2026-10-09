/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.audit;

import java.util.List;

import org.jboss.pnc.model.GenericEntity;

/**
 * Audited repository type.
 *
 * @param <Entity> Type of the audited entity.
 * @param <ID> Type of audited entity id.
 */
public interface AuditRepository<Entity extends GenericEntity<ID>, ID extends Number> {

    /**
     * Gets all revisions for audited entity.
     * 
     * @return A list of all revisions of this entity
     */
    List<Revision<Entity, ID>> getAllRevisions();
}
