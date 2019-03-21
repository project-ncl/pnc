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
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenDeleting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.facade.validation.DTOValidationException;

import com.google.common.collect.ObjectArrays;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pnc.facade.mapper.api.EntityMapper;
import org.jboss.pnc.facade.validation.ValidationBuilder;

import javax.inject.Inject;

import org.jboss.pnc.facade.rsql.RSQLProducer;

/**
 * Abstract provider with common functionality.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Sebastian Laskawiec
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
public abstract class AbstractProvider<DB extends GenericEntity<Integer>, DTO extends REF, REF extends DTOEntity> implements Provider<DB, DTO, REF> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProvider.class);

    @Inject
    protected RSQLProducer rsqlPredicateProducer;

    @Inject
    protected PageInfoProducer pageInfoProducer;

    protected Repository<DB, Integer> repository;

    protected EntityMapper<DB, DTO, REF> mapper;

    protected final Class<DB> type;

    public AbstractProvider(Repository<DB, Integer> repository, EntityMapper<DB, DTO, REF> mapper, Class<DB> type) {
        this.repository = repository;
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    public DTO store(DTO restEntity) throws DTOValidationException {
        validateBeforeSaving(restEntity);
        log.debug("Storing entity: " + restEntity.toString());
        DB storedEntity = repository.save(mapper.toEntity(restEntity));
        return mapper.toDTO(storedEntity);
    }

    @Override
    public DTO getSpecific(Integer id) {
        DB dbEntity = repository.queryById(id);
        return mapper.toDTO(dbEntity);
    }

    @Override
    public Page<DTO> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query);
    }

    @Override
    public void update(Integer id, DTO restEntity) {
        validateBeforeUpdating(id, restEntity);
        log.debug("Updating entity: " + restEntity.toString());
        repository.save(mapper.toEntity(restEntity));
    }

    @Override
    public void delete(Integer id) {
        validateBeforeDeleting(id);
        repository.delete(id);
    }

    @Override
    public Page<DTO> queryForCollection(int pageIndex, int pageSize, String sortingRsql, String query,
            Predicate<DB>... predicates) {
        Predicate<DB> rsqlPredicate = rsqlPredicateProducer.getCriteriaPredicate(type, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = rsqlPredicateProducer.getSortInfo(type, sortingRsql);
        List<DB> collection = repository.queryWithPredicates(pageInfo, sortInfo, ObjectArrays.concat(rsqlPredicate, predicates));
        int totalHits = repository.count(ObjectArrays.concat(rsqlPredicate, predicates));
        int totalPages = (totalHits + pageSize - 1) / pageSize;
        List<DTO> content = nullableStreamOf(collection)
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return new Page<>(pageIndex, pageSize, totalPages, totalHits, content);
    }

    protected void validateBeforeUpdating(Integer id, DTO restEntity) {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(repository, id, true);
    }

    protected void validateBeforeSaving(DTO restEntity) {
        ValidationBuilder.validateObject(restEntity, WhenCreatingNew.class)
                .validateNotEmptyArgument().validateAnnotations();
    }

    protected void validateBeforeDeleting(Integer id) {
        ValidationBuilder.validateObject(WhenDeleting.class)
                .validateAgainstRepository(repository, id, true)
                .validateAnnotations();
    }
}
