/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import com.google.common.collect.ObjectArrays;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.rest.endpoint.AbstractEndpoint;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.GenericRestEntity;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenDeleting;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Function;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

/**
 * Abstract provider with common functionality.
 *
 * @author Sebastian Laskawiec
 */
public abstract class AbstractProvider<DBEntity extends GenericEntity<Integer>, RESTEntity extends GenericRestEntity<Integer>> {

    private static final Logger log = LoggerFactory.getLogger(AbstractEndpoint.class);

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

    public CollectionInfo<RESTEntity> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, null);
    }

    public CollectionInfo<RESTEntity> queryForCollection(int pageIndex, int pageSize, String sortingRsql, String query,
            Predicate<DBEntity>... predicates) {
        Predicate<DBEntity> rsqlPredicate = rsqlPredicateProducer.getPredicate(getDBEntityClass(), query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        List<DBEntity> collection;
        int totalPages;
        if(predicates == null) {
            collection = repository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate);
            totalPages = (repository.count(rsqlPredicate) + pageSize - 1) / pageSize;
        } else {
            collection = repository.queryWithPredicates(pageInfo, sortInfo, ObjectArrays.concat(rsqlPredicate, predicates));
            totalPages = (repository.count(ObjectArrays.concat(rsqlPredicate, predicates)) + pageSize - 1) / pageSize;
        }

        return nullableStreamOf(collection)
                .map(toRESTModel())
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize, totalPages));
    }

    public RESTEntity getSpecific(Integer id) {
        DBEntity dbEntity = repository.queryById(id);
        if (dbEntity != null) {
            return toRESTModel().apply(dbEntity);
        }
        return null;
    }

    public Integer store(RESTEntity restEntity) throws RestValidationException {
        return store(restEntity, true);
    }

    public Integer store(RESTEntity restEntity, boolean validateBeforeSaving) throws RestValidationException {
        if (validateBeforeSaving) {
            validateBeforeSaving(restEntity);
        }
        log.debug("Storing entity: " + restEntity.toString());
        return repository.save(toDBModel().apply(restEntity)).getId();
    }

    public void update(Integer id, RESTEntity restEntity) throws RestValidationException {
        restEntity.setId(id);
        validateBeforeUpdating(id, restEntity);
        log.debug("Updating entity: " + restEntity.toString());
        repository.save(toDBModel().apply(restEntity));
    }

    public void delete(Integer id) throws RestValidationException {
        validateBeforeDeleting(id);
        repository.delete(id);
    }

    protected void validateBeforeUpdating(Integer id, RESTEntity restEntity) throws RestValidationException {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(repository, id, true);
    }

    protected void validateBeforeSaving(RESTEntity restEntity) throws RestValidationException {
        ValidationBuilder.validateObject(restEntity, WhenCreatingNew.class)
                .validateNotEmptyArgument().validateAnnotations();
    }

    protected void validateBeforeDeleting(Integer id) throws RestValidationException {
        ValidationBuilder.validateObject(WhenDeleting.class)
                .validateAgainstRepository(repository, id, true)
                .validateAnnotations();
    }

    protected abstract Function<? super DBEntity, ? extends RESTEntity> toRESTModel();

    protected abstract Function<? super RESTEntity, ? extends DBEntity> toDBModel();

    public Class<DBEntity> getDBEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DBEntity>) superclass.getActualTypeArguments()[0];
    }

    public Class<DBEntity> getRESTEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DBEntity>) superclass.getActualTypeArguments()[1];
    }

}
