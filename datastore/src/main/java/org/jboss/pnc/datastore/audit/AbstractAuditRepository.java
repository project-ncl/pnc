/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.audit;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQuery;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.audit.AuditRepository;
import org.jboss.pnc.spi.datastore.audit.Revision;

public abstract class AbstractAuditRepository<Entity extends GenericEntity<ID>, ID extends Number>
        implements AuditRepository<Entity, ID> {

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
        return new Revision<>() {
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
