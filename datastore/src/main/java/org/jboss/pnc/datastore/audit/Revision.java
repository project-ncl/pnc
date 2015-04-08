package org.jboss.pnc.datastore.audit;

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
     */
    ID getId();

    /**
     * Returns audited entity.
     */
    Entity getAuditedEntity();

}
