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
package org.jboss.pnc.spi.datastore.audit;

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
     * 
     * @return A list of all revisions of this entity
     */
    List<Revision<Entity, ID>> getAllRevisions();
}
