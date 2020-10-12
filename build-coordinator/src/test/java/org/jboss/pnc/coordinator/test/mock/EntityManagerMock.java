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
package org.jboss.pnc.coordinator.test.mock;

import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
public class EntityManagerMock implements EntityManager {

    @Override
    public void persist(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T merge(T entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void detach(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createQuery(String qlString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNamedQuery(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void joinTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isJoinedToTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getDelegate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
