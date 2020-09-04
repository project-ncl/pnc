/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class AbstractRepository<T extends GenericEntity<ID>, ID extends Serializable> implements Repository<T, ID> {

    @Inject
    protected EntityManager entityManager;

    protected JpaRepository<T, ID> springRepository;

    protected JpaSpecificationExecutor<T> springSpecificationsExecutor;

    public AbstractRepository() {
    }

    public AbstractRepository(
            JpaRepository<T, ID> springRepository,
            JpaSpecificationExecutor<T> springSpecificationsExecutor) {
        this.springRepository = springRepository;
        this.springSpecificationsExecutor = springSpecificationsExecutor;
    }

    @Override
    public T save(T entity) {
        return springRepository.save(entity);
    }

    @Override
    public void delete(ID id) {
        springRepository.delete(id);
    }

    @Override
    public void flushAndRefresh(T entity) {
        springRepository.flush();
        entityManager.refresh(entity);
    }

    @Override
    public List<T> queryAll() {
        return springRepository.findAll();
    }

    @Override
    public List<T> queryAll(PageInfo pageInfo, SortInfo sortInfo) {
        return springRepository.findAll(PageableMapper.map(pageInfo, sortInfo)).getContent();
    }

    @Override
    public T queryById(ID id) {
        return springRepository.findOne(id);
    }

    @Override
    public T queryByPredicates(Predicate<T>... predicates) {
        return springSpecificationsExecutor.findOne(SpecificationsMapper.map(predicates));
    }

    @Override
    public int count(Predicate<T>... predicates) {
        long countAsLong = springSpecificationsExecutor.count(SpecificationsMapper.map(predicates));
        return (int) countAsLong;
    }

    @Override
    public int count(Collection<Predicate<T>> andPredicates, Collection<Predicate<T>> orPredicates) {
        long countAsLong = springSpecificationsExecutor.count(SpecificationsMapper.map(andPredicates, orPredicates));
        return (int) countAsLong;
    }

    @Override
    public List<T> queryWithPredicates(Predicate<T>... predicates) {
        return springSpecificationsExecutor.findAll(SpecificationsMapper.map(predicates));
    }

    @Override
    public List<T> queryWithPredicates(PageInfo pageInfo, SortInfo sortInfo, Predicate<T>... predicates) {
        return springSpecificationsExecutor
                .findAll(SpecificationsMapper.map(predicates), PageableMapper.map(pageInfo, sortInfo))
                .getContent();
    }

    /**
     * @see Repository#cascadeUpdates) for full docs
     * 
     * @param managedNonOwning current version of entity from DB (MUST be Hibernate managed (due to LAZY fetching))
     * @param updatedNonOwning proposed version of entity from request
     * @param collectionGetter getter with collection of owning side (f.e ProductVersion::getBuildConfigurations)
     * @param owningSetter setter which updates the owning side (f.e BuildConfiguration::setProductVersion)
     * @param filter
     * @param <N>
     */
    public <N extends GenericEntity<ID>> void cascadeUpdates(
            N managedNonOwning,
            N updatedNonOwning,
            Function<N, Collection<T>> collectionGetter,
            BiConsumer<T, N> owningSetter,
            java.util.function.Predicate<T>... filter) {
        Collection<T> original = collectionGetter.apply(managedNonOwning);
        Collection<T> updated = collectionGetter.apply(updatedNonOwning);

        Map<ID, T> toRemove = new HashMap<>();
        insertToMap(original, toRemove);
        removeFromMap(updated, toRemove);

        for (T owning : toRemove.values()) {
            owningSetter.accept(owning, null);
            save(owning);
        }

        Map<ID, T> toAdd = new HashMap<>();
        insertToMap(updated, toAdd);
        removeFromMap(original, toAdd);

        java.util.function.Predicate<T> dontMatchAll = Arrays.stream(filter)
                .reduce(x -> true, java.util.function.Predicate::and)
                .negate();

        // the entry value has to match all the filters (remove if it doesn't match all)
        toAdd.values().removeIf(dontMatchAll);

        for (T owning : toAdd.values()) {
            // get full entity to avoid saving partial data from request(due to refs in dto maps)
            T fullEntity = queryById(owning.getId());
            owningSetter.accept(fullEntity, managedNonOwning);
            save(fullEntity);
        }
    }

    private void insertToMap(Collection<T> entityCollection, Map<ID, T> map) {
        map.putAll(entityCollection.stream().collect(toMap(T::getId, id -> id)));
    }

    private void removeFromMap(Collection<T> entityCollection, Map<ID, T> map) {
        map.keySet().removeAll(entityCollection.stream().map(T::getId).collect(toSet()));
    }

}
