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

import com.google.common.collect.ObjectArrays;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.rest.facade.mappers.api.EntityMapper;
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

import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.provider.api.Provider;

/**
 * Abstract provider with common functionality.
 *
 * @author Sebastian Laskawiec
 */
public abstract class AbstractProvider<DB extends GenericEntity<Integer>, Rest> implements Provider<DB, Rest> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProvider.class);

    protected RSQLPredicateProducer rsqlPredicateProducer;

    protected SortInfoProducer sortInfoProducer;

    protected PageInfoProducer pageInfoProducer;

    protected Repository<DB, Integer> repository;

    protected EntityMapper<DB, Rest, ?> mapper;


    public AbstractProvider(Repository<DB, Integer> repository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        this.repository = repository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    @Override
    public CollectionInfo<Rest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, new Predicate[0]);
    }

    @Override
    public CollectionInfo<Rest> queryForCollection(int pageIndex, int pageSize, String sortingRsql, String query,
            Predicate<DB>... predicates) {
        Predicate<DB> rsqlPredicate = rsqlPredicateProducer.getPredicate(getDBEntityClass(), query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        List<DB> collection;
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

    @Override
    public Rest getSpecific(Integer id) {
        DB dbEntity = repository.queryById(id);
        if (dbEntity != null) {
            return toRESTModel().apply(dbEntity);
        }
        return null;
    }

    @Override
    public Integer store(Rest restEntity) throws RestValidationException {
        validateBeforeSaving(restEntity);
        log.debug("Storing entity: " + restEntity.toString());
        return repository.save(toDBModel().apply(restEntity)).getId();
    }

    @Override
    public void update(Integer id, Rest restEntity) throws RestValidationException {
        validateBeforeUpdating(id, restEntity);
        log.debug("Updating entity: " + restEntity.toString());
        repository.save(toDBModel().apply(restEntity));
    }

    @Override
    public void delete(Integer id) throws RestValidationException {
        validateBeforeDeleting(id);
        repository.delete(id);
    }

    protected void validateBeforeUpdating(Integer id, Rest restEntity) throws RestValidationException {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(repository, id, true);
    }

    protected void validateBeforeSaving(Rest restEntity) throws RestValidationException {
        ValidationBuilder.validateObject(restEntity, WhenCreatingNew.class)
                .validateNotEmptyArgument().validateAnnotations();
    }

    protected void validateBeforeDeleting(Integer id) throws RestValidationException {
        ValidationBuilder.validateObject(WhenDeleting.class)
                .validateAgainstRepository(repository, id, true)
                .validateAnnotations();
    }

    protected Function<? super DB, ? extends Rest> toRESTModel() {
        return mapper::toRest;
    }

    protected Function<? super Rest, ? extends DB> toDBModel() {
        return mapper::toEntity;
    }

    public Class<DB> getDBEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DB>) superclass.getActualTypeArguments()[0];
    }

    public Class<DB> getRESTEntityClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<DB>) superclass.getActualTypeArguments()[1];
    }

}
