/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.model.event;

/**
 * @author Jakub Senko
 */
public class EntityUpdateEvent {

    private final Integer entityId;
    private final Class<?> entityClass;
    private final EntityOperationType operationType;

    public EntityUpdateEvent(Integer entityId, Class<?> entityClass, EntityOperationType operationType) {
        this.entityId = entityId;
        this.entityClass = entityClass;
        this.operationType = operationType;
    }


    public Integer getEntityId() {
        return entityId;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public EntityOperationType getOperationType() {
        return operationType;
    }
}
