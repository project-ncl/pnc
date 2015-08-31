/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.rest.restmodel.GenericRestEntity;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

/**
 * Abstract provider with common functionality.
 *
 * @author Sebastian Laskawiec
 */
public abstract class AbstractProvider<DBEntity extends GenericEntity<Integer>, RESTEntity extends GenericRestEntity<Integer>> {

    protected RSQLPredicateProducer rsqlPredicateProducer;

    protected SortInfoProducer sortInfoProducer;

    protected PageInfoProducer pageInfoProducer;

    protected Repository<DBEntity, Integer> repository;

    @Deprecated
    public AbstractProvider() {
    }

    public AbstractProvider(Repository<DBEntity, Integer> repository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        this.repository = repository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<RESTEntity> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, null);
    }

    public List<RESTEntity> queryForCollection(int pageIndex, int pageSize, String sortingRsql, String query,
            Predicate<DBEntity>... predicates) {
        Predicate<DBEntity> rsqlPredicate = rsqlPredicateProducer.getPredicate(getDBEntityClass(), query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        List<DBEntity> collection;
        if(predicates == null) {
            collection = repository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate);
        } else {
            collection = repository.queryWithPredicates(pageInfo, sortInfo, ObjectArrays.concat(rsqlPredicate, predicates));
        }

        return nullableStreamOf(collection)
                .map(toRESTModel())
                .collect(Collectors.toList());
    }

    public RESTEntity getSpecific(Integer id) {
        DBEntity dbEntity = repository.queryById(id);
        if (dbEntity != null) {
            return toRESTModel().apply(dbEntity);
        }
        return null;
    }

    public Integer store(RESTEntity licenseRest) throws ConflictedEntryException {
        validateBeforeSaving(licenseRest);
        return repository.save(toDBModelModel().apply(licenseRest)).getId();
    }

    public void update(Integer id, RESTEntity licenseRest) throws ConflictedEntryException {
        validateBeforeUpdating(id, licenseRest);
        DBEntity license = repository.queryById(id);
        Preconditions.checkArgument(license != null, "Couldn't find entity with id " + id);
        licenseRest.setId(id);
        repository.save(toDBModelModel().apply(licenseRest));
    }

    public void delete(Integer id) {
        validateBeforeDeleting(id);
        repository.delete(id);
    }

    protected void validateBeforeUpdating(Integer id, RESTEntity restEntity) throws ConflictedEntryException {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(restEntity.getId() == null || restEntity.getId().equals(id),
                "Entity id does not match the id to update");
    }

    protected void validateBeforeSaving(RESTEntity restEntity) throws ConflictedEntryException {
        Preconditions.checkArgument(restEntity.getId() == null, "Id must be null");
    }

    protected void validateBeforeDeleting(Integer id) {
        Preconditions.checkArgument(repository.queryById(id) != null, "Couldn't find entity with id " + id);
    }

    protected abstract Function<? super DBEntity, ? extends RESTEntity> toRESTModel();

    protected abstract Function<? super RESTEntity, ? extends DBEntity> toDBModelModel();

    public Class<DBEntity> getDBEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DBEntity>) superclass.getActualTypeArguments()[0];
    }

    public Class<DBEntity> getRESTEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DBEntity>) superclass.getActualTypeArguments()[1];
    }

}
