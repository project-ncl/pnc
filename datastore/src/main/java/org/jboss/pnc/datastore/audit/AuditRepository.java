package org.jboss.pnc.datastore.audit;

import org.jboss.pnc.model.GenericEntity;

import java.util.List;

/**
 * Audited repository type.
 *
 * @param <Entity> Type of the audited entity.
 * @param <ID> Type of audited entity id.
 */
public interface AuditRepository<Entity extends GenericEntity<ID>, ID extends Number> {

    /**
     * Gets all revisions for audited entity.
     */
    List<Revision<Entity, ID>> getAllRevisions();
}
