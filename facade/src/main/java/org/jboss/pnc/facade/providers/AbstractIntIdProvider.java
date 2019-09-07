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

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.validation.groups.WhenDeleting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.providers.api.Provider;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.EntityMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

/**
 * Abstract provider with common functionality.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Sebastian Laskawiec
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
@PermitAll //required to allow all non explicitly restricted operations in EJB that use other restrictions
//TODO ID Converter ?
public abstract class AbstractIntIdProvider<DB extends GenericEntity<Integer>, DTO extends REF, REF extends DTOEntity>
        extends AbstractProvider<Integer, DB, DTO, REF>
        implements Provider<Integer, DB, DTO, REF> {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntIdProvider.class);

    public AbstractIntIdProvider(Repository<DB, Integer> repository, EntityMapper<Integer, DB, DTO, REF> mapper, Class<DB> type) {
        super(repository, mapper, type);
    }

    @Override
    public DTO getSpecific(String id) {
        DB dbEntity = repository.queryById(Integer.valueOf(id));
        return mapper.toDTO(dbEntity);
    }

    @Override
    public void delete(String id) {
        validateBeforeDeleting(id);
        repository.delete(Integer.valueOf(id));
    }

    protected void validateBeforeUpdating(String id, DTO restEntity) {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class)
                .validateNotEmptyArgument()
                .validateAnnotations()
                .validateAgainstRepository(repository, Integer.valueOf(id), true);
    }

    protected void validateBeforeDeleting(String id) {
        ValidationBuilder.validateObject(WhenDeleting.class)
                .validateAgainstRepository(repository, Integer.valueOf(id), true)
                .validateAnnotations();
    }
}
