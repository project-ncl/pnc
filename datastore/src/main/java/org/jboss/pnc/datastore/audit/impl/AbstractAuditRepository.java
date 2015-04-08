package org.jboss.pnc.datastore.audit.impl;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQuery;
import org.jboss.pnc.datastore.audit.AuditRepository;
import org.jboss.pnc.datastore.audit.Revision;
import org.jboss.pnc.model.GenericEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAuditRepository<Entity extends GenericEntity<ID>, ID extends Number> implements AuditRepository<Entity, ID> {

    protected AuditReader auditReader;
    protected Class<Entity> entityClass;

    public AbstractAuditRepository(AuditReader auditReader, Class<Entity> entityClass) {
        this.auditReader = auditReader;
        this.entityClass = entityClass;
    }

    @Override
    public List<Revision<Entity, ID>> getAllRevisions() {
        List<Revision<Entity, ID>> returnedRevisions = new ArrayList<>();
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(entityClass, true, true);
        query.getResultList().forEach(returnedEntity -> returnedRevisions.add(createRevision(returnedEntity)));
        return returnedRevisions;
    }

    protected Revision<Entity, ID> createRevision(Object returnedEntity) {
        Entity castedEntity = (Entity) returnedEntity;
        return new Revision<Entity, ID>() {
            @Override
            public ID getId() {
                return castedEntity.getId();
            }

            @Override
            public Entity getAuditedEntity() {
                return castedEntity;
            }
        };
    }

}
