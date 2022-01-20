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
package org.jboss.pnc.facade.providers;

import java.io.Serializable;
import javax.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.UpdatableEntityMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Extension of abstract provider adding update functionality.
 *
 * @author jbrazdil
 *
 * @param <ID> The type of the entity identifier
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
@Slf4j
@PermitAll
public abstract class AbstractUpdatableProvider<ID extends Serializable, DB extends GenericEntity<ID>, DTO extends REF, REF extends DTOEntity>
        extends AbstractProvider<ID, DB, DTO, REF> {

    public AbstractUpdatableProvider(
            Repository<DB, ID> repository,
            UpdatableEntityMapper<ID, DB, DTO, REF> mapper,
            Class<DB> type) {
        super(repository, mapper, type);
    }

    @Override
    public UpdatableEntityMapper<ID, DB, DTO, REF> mapper() {
        return (UpdatableEntityMapper<ID, DB, DTO, REF>) mapper;
    }

    @Override
    public DTO update(String stringId, DTO restEntity) {
        ID id = parseId(stringId);
        validateBeforeUpdating(id, restEntity);
        log.debug("Updating entity: " + restEntity.toString());
        DB dbEntity = repository.queryById(id);
        log.debug("Updating existing entity: " + dbEntity);
        preUpdate(dbEntity, restEntity);
        mapper().updateEntity(restEntity, dbEntity);
        onUpdate(dbEntity);
        log.debug("Updated entity: " + dbEntity);
        return mapper().toDTO(dbEntity);
    }

    protected DB findInDB(ID id) {
        DB dbEntity = repository.queryById(id);
        if (dbEntity == null) {
            throw new RepositoryViolationException("Entity should exist in the DB.");
        }
        return dbEntity;
    }

    protected void validateBeforeUpdating(ID id, DTO restEntity) {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(repository, id, true);
    }

    protected void onUpdate(DB dbEntity) {
        // no-op
    }

    protected void preUpdate(DB dbEntity, DTO restEntity) {
        // no-op
    }
}
