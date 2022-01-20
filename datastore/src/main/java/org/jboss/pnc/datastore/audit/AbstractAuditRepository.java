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
package org.jboss.pnc.datastore.audit;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQuery;
import org.jboss.pnc.spi.datastore.audit.AuditRepository;
import org.jboss.pnc.spi.datastore.audit.Revision;
import org.jboss.pnc.model.GenericEntity;

import java.util.ArrayList;
import java.util.List;

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
