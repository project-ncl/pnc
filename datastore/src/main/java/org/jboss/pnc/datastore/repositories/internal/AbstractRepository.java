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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.OrderInfo;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author Part of this file is taken and modified from the spring-data-jpa
 *         (https://github.com/spring-projects/spring-data-jpa) and spring-data-commons
 *         (https://github.com/spring-projects/spring-data-commons) projects, both are licensed under the Apache
 *         License, Version 2.0.
 */
public abstract class AbstractRepository<T extends GenericEntity<ID>, ID extends Serializable>
        implements Repository<T, ID> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRepository.class);

    @Inject
    protected EntityManager entityManager;

    protected final Class<T> entityClass;
    protected final Class<ID> idClass;

    protected AbstractRepository(Class<T> entityClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.idClass = idClass;
    }

    @Override
    public T save(T entity) {
        if (isNew(entity)) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Override
    public void delete(ID id) {
        Objects.requireNonNull(id, "The given id must not be null!");
        T entity = this.queryById(id);
        if (entity != null) {
            this.delete(entity);
        }
    }

    @Override
    public void delete(T entity) {
        Objects.requireNonNull(entity, "The entity must not be null!");
        if (isNew(entity)) {
            return;
        }

        T existing = (T) entityManager.find(entityClass, entity.getId());

        // if the entity to be deleted doesn't exist, delete is a NOOP
        if (existing == null) {
            return;
        }

        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    public void flushAndRefresh(T entity) {
        entityManager.flush();
        entityManager.refresh(entity);
    }

    @Override
    public List<T> queryAll() {
        return getQuery().getResultList();
    }

    @Override
    public List<T> queryAll(PageInfo pageInfo, SortInfo<T> sortInfo) {
        return findAll(pageInfo, sortInfo);
    }

    @Override
    public T queryById(ID id) {
        Objects.requireNonNull(id, "The given id must not be null!");
        return this.entityManager.find(entityClass, id);
    }

    @Override
    public T queryByPredicates(Predicate<T>... predicates) {
        try {
            return getQuery(null, predicates).setMaxResults(2).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public int count(Predicate<T>... predicates) {
        long countAsLong = _count(Arrays.asList(predicates), Collections.emptyList());
        if (countAsLong > Integer.MAX_VALUE) {
            log.error("Trimming count exceeding Integer.MAX_VALUE: " + countAsLong);
            return Integer.MAX_VALUE;
        }
        return (int) countAsLong;
    }

    @Override
    public int count(Collection<Predicate<T>> andPredicates, Collection<Predicate<T>> orPredicates) {
        long countAsLong = _count(andPredicates, orPredicates);
        if (countAsLong > Integer.MAX_VALUE) {
            log.error("Trimming count exceeding Integer.MAX_VALUE: " + countAsLong);
            return Integer.MAX_VALUE;
        }
        return (int) countAsLong;
    }

    @Override
    public List<T> queryWithPredicates(Predicate<T>... predicates) {
        return findAll(null, null, predicates);
    }

    @Override
    public List<T> queryWithPredicates(PageInfo pageInfo, SortInfo<T> sortInfo, Predicate<T>... predicates) {
        return findAll(pageInfo, sortInfo, predicates);
    }

    /**
     * @param managedNonOwning current version of entity from DB (MUST be Hibernate managed (due to LAZY fetching))
     * @param updatedNonOwning proposed version of entity from request
     * @param collectionGetter getter with collection of owning side (f.e ProductVersion::getBuildConfigurations)
     * @param owningSetter setter which updates the owning side (f.e BuildConfiguration::setProductVersion)
     * @param filter
     * @param <N>
     * @see Repository#cascadeUpdates) for full docs
     */
    @Override
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

    private boolean isNew(T entity) {
        ID id = entity.getId();

        if (!idClass.isPrimitive()) {
            return id == null;
        }

        if (id instanceof Number) {
            return ((Number) id).longValue() == 0L;
        }

        throw new IllegalArgumentException(String.format("Unsupported primitive id type %s", idClass));
    }

    private List<T> findAll(PageInfo pageInfo, SortInfo<T> sortInfo, Predicate<T>... predicates) {
        TypedQuery<T> query = getQuery(sortInfo, predicates);
        if (pageInfo != null) {
            query.setFirstResult(pageInfo.getElementOffset());
            query.setMaxResults(pageInfo.getPageSize());
        }
        return query.getResultList();
    }

    private long _count(Collection<Predicate<T>> andPredicates, Collection<Predicate<T>> orPredicates) {
        return executeCountQuery(getCountQuery(andPredicates, orPredicates));
    }

    private long executeCountQuery(TypedQuery<Long> query) {
        Objects.requireNonNull(query, "TypedQuery must not be null");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }

    private TypedQuery<Long> getCountQuery(
            Collection<Predicate<T>> andPredicates,
            Collection<Predicate<T>> orPredicates) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<T> root = applySpecificationToCriteria(query, andPredicates, orPredicates);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.emptyList());

        return entityManager.createQuery(query);
    }

    private TypedQuery<T> getQuery() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);

        Root<T> root = query.from(entityClass);
        query.select(root);

        return entityManager.createQuery(query);
    }

    private TypedQuery<T> getQuery(SortInfo<T> sortInfo, Predicate<T>... predicates) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);

        Root<T> root = applySpecificationToCriteria(query, predicates);
        query.select(root);

        if (sortInfo != null) {
            query.orderBy(toOrders(sortInfo, root, builder));
        }

        return entityManager.createQuery(query);
    }

    private TypedQuery<T> getQuery(
            SortInfo<T> sortInfo,
            Collection<Predicate<T>> andPredicates,
            Collection<Predicate<T>> orPredicates) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(entityClass);

        Root<T> root = applySpecificationToCriteria(query, andPredicates, orPredicates);
        query.select(root);

        if (sortInfo != null) {
            query.orderBy(toOrders(sortInfo, root, builder));
        }

        return entityManager.createQuery(query);
    }

    private <S> Root<T> applySpecificationToCriteria(CriteriaQuery<S> query, Predicate<T>... predicates) {
        Objects.requireNonNull(query, "CriteriaQuery must not be null");

        Root<T> root = query.from(entityClass);

        if (predicates.length == 0) {
            return root;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        var jpaPredicates = Arrays.stream(predicates)
                .map(p -> p.apply(root, query, builder))
                .collect(Collectors.toList())
                .toArray(new javax.persistence.criteria.Predicate[predicates.length]);

        query.where(builder.and(jpaPredicates));
        return root;
    }

    private <S> Root<T> applySpecificationToCriteria(
            CriteriaQuery<S> query,
            Collection<Predicate<T>> andPredicates,
            Collection<Predicate<T>> orPredicates) {
        Objects.requireNonNull(query, "CriteriaQuery must not be null");
        Objects.requireNonNull(andPredicates, "andPredicates must not be null");
        Objects.requireNonNull(orPredicates, "orPredicates must not be null");

        Root<T> root = query.from(entityClass);

        if (andPredicates.isEmpty() && orPredicates.isEmpty()) {
            return root;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        var jpaAndPredicates = andPredicates.stream()
                .map(p -> p.apply(root, query, builder))
                .collect(Collectors.toList())
                .toArray(new javax.persistence.criteria.Predicate[andPredicates.size()]);
        var jpaOrPredicates = orPredicates.stream()
                .map(p -> p.apply(root, query, builder))
                .collect(Collectors.toList())
                .toArray(new javax.persistence.criteria.Predicate[orPredicates.size()]);

        javax.persistence.criteria.Predicate predicate;
        if (andPredicates.isEmpty()) {
            if (orPredicates.isEmpty())
                predicate = builder.conjunction();
            else {
                predicate = builder.or(jpaOrPredicates);
            }
        } else {
            if (orPredicates.isEmpty())
                predicate = builder.and(jpaAndPredicates);
            else {
                predicate = builder.or(builder.and(jpaAndPredicates), builder.or(jpaOrPredicates));
            }
        }

        query.where(predicate);
        return root;
    }

    private List<Order> toOrders(SortInfo<T> sort, Root<T> from, CriteriaBuilder cb) {
        if (sort == null) {
            return Collections.emptyList();
        }

        Objects.requireNonNull(from, "From must not be null");
        Objects.requireNonNull(cb, "CriteriaBuilder must not be null");

        List<Order> orders = new ArrayList<>();

        for (OrderInfo<T> order : sort.orders()) {
            orders.add(toJpaOrder(order, from, cb));
        }

        return orders;
    }

    private Order toJpaOrder(OrderInfo<T> order, Root<T> from, CriteriaBuilder cb) {
        Expression<?> expression = order.getExpression(from);
        return order.getDirection() == OrderInfo.SortingDirection.ASC ? cb.asc(expression) : cb.desc(expression);
    }
}
