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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/29/16 Time: 7:03 AM
 */
@SuppressWarnings({ "WeakerAccess", "unchecked" })
public abstract class RepositoryMock<ID extends Serializable, EntityType extends GenericEntity<ID>>
        implements Repository<EntityType, ID> {
    protected final List<EntityType> data = new CopyOnWriteArrayList<>();

    @Override
    public EntityType save(EntityType entity) {
        ID id = entity.getId();
        if (id == null) {
            entity.setId(getNextId());
            id = entity.getId();
        }
        getOptionalById(id).ifPresent(data::remove);
        data.add(entity);
        return entity;
    }

    public abstract ID getNextId();

    @Override
    public List<EntityType> queryAll() {
        return data;
    }

    @Override
    public List<EntityType> queryAll(PageInfo pageInfo, SortInfo<EntityType> sortInfo) {
        return Collections.emptyList();
    }

    @Override
    public List<EntityType> queryWithPredicates(
            PageInfo pageInfo,
            SortInfo<EntityType> sortInfo,
            Predicate<EntityType>... predicates) {
        return Collections.emptyList();
    }

    @Override
    public List<EntityType> queryWithPredicates(Predicate<EntityType>... predicates) {
        return Collections.emptyList();
    }

    @Override
    public int count(Predicate<EntityType>... predicates) {
        return 0;
    }

    @Override
    public int count(Collection<Predicate<EntityType>> andPredicates, Collection<Predicate<EntityType>> orPredicates) {
        return 0;
    }

    @Override
    public EntityType queryByPredicates(Predicate<EntityType>... predicates) {
        return null;
    }

    private Optional<EntityType> getOptionalById(ID id) {
        return data.stream().filter(m -> id.equals(m.getId())).findAny();
    }

    @Override
    public EntityType queryById(ID id) {
        return getOptionalById(id).orElse(null);
    }

    @Override
    public void delete(ID id) {
        data.removeIf(e -> id.equals(e.getId()));
    }

    @Override
    public void delete(EntityType entity) {
        delete(entity.getId());
    }

    @Override
    public void flushAndRefresh(EntityType entity) {
    }

    @Override
    public <D extends GenericEntity<ID>> void cascadeUpdates(
            D managedNonOwning,
            D updatedNonOwning,
            Function<D, Collection<EntityType>> collectionGetter,
            BiConsumer<EntityType, D> owningSetter,
            java.util.function.Predicate<EntityType>... filters) {
        throw new UnsupportedOperationException("Not implemented, don't use");
    }

    public void clear() {
        data.clear();
    }
}
