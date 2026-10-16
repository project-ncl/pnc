/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.audit;

import org.jboss.pnc.model.GenericEntity;

/**
 * Single audited revision.
 *
 * @param <Entity> Type of the entity.
 * @param <ID> Type of entity's id.
 */
public interface Revision<Entity extends GenericEntity<ID>, ID extends Number> {

    /**
     * Returns entity's id value.
     * 
     * @return The entity ID
     */
    ID getId();

    /**
     * Returns audited entity.
     * 
     * @return The audited entity
     */
    Entity getAuditedEntity();

}
