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
package org.jboss.pnc.facade.providers;

import com.google.common.collect.ObjectArrays;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenDeleting;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.facade.rsql.RSQLProducer;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.EntityMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

/**
 * Abstract provider with common functionality.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Sebastian Laskawiec
 * @param <ID> The type of the entity identifier
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
@PermitAll // required to allow all non explicitly restricted operations in EJB that use other restrictions
public abstract class AbstractProvider<ID extends Serializable, DB extends GenericEntity<ID>, DTO extends REF, REF extends DTOEntity>
        implements Provider<ID, DB, DTO, REF> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProvider.class);

    @Inject
    protected RSQLProducer rsqlPredicateProducer;

    @Inject
    protected PageInfoProducer pageInfoProducer;

    protected Repository<DB, ID> repository;

    protected EntityMapper<ID, DB, DTO, REF> mapper;

    protected final Class<DB> type;

    public AbstractProvider(Repository<DB, ID> repository, EntityMapper<ID, DB, DTO, REF> mapper, Class<DB> type) {
        this.repository = repository;
        this.mapper = mapper;
        this.type = type;
    }

    public EntityMapper<ID, DB, DTO, REF> mapper() {
        return mapper;
    }

    @Override
    public DTO getSpecific(String id) {
        DB dbEntity = repository.queryById(mapper.getIdMapper().toEntity(id));
        return mapper.toDTO(dbEntity);
    }

    @Override
    public DTO store(DTO restEntity) throws DTOValidationException {
        return store(restEntity, true);
    }

    protected DTO store(DTO restEntity, boolean validateBeforeSaving) throws DTOValidationException {
        if (validateBeforeSaving) {
            validateBeforeSaving(restEntity);
        }
        log.debug("Storing entity: " + restEntity.toString());
        DB storedEntity = repository.save(mapper.toEntity(restEntity));
        repository.flushAndRefresh(storedEntity);
        return mapper.toDTO(storedEntity);
    }

    @Override
    public Page<DTO> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query);
    }

    @Override
    public DTO update(String id, DTO restEntity) {
        throw new UnsupportedOperationException("Update operation not supported.");
    }

    @Override
    public void delete(String id) {
        validateBeforeDeleting(id);
        repository.delete(mapper.getIdMapper().toEntity(id));
    }

    @Override
    public Page<DTO> queryForCollection(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Predicate<DB>... predicates) {
        Predicate<DB> rsqlPredicate = rsqlPredicateProducer.getCriteriaPredicate(type, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = rsqlPredicateProducer.getSortInfo(type, sortingRsql);
        List<DB> collection = repository
                .queryWithPredicates(pageInfo, sortInfo, ObjectArrays.concat(rsqlPredicate, predicates));
        int totalHits = repository.count(ObjectArrays.concat(rsqlPredicate, predicates));
        int totalPages = (totalHits + pageSize - 1) / pageSize;
        List<DTO> content = nullableStreamOf(collection).map(mapper::toDTO).collect(Collectors.toList());
        return new Page<>(pageIndex, pageSize, totalPages, totalHits, content);
    }

    protected void validateBeforeSaving(DTO restEntity) {
        ValidationBuilder.validateObject(restEntity, WhenCreatingNew.class)
                .validateNotEmptyArgument()
                .validateAnnotations();
    }

    protected void validateBeforeDeleting(String id) {
        ValidationBuilder.validateObject(WhenDeleting.class)
                .validateAgainstRepository(repository, mapper.getIdMapper().toEntity(id), true)
                .validateAnnotations();
    }
}
