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

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.jboss.pnc.dto.Operation;
import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.facade.providers.api.OperationProvider;
import org.jboss.pnc.mapper.api.UpdatableEntityMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import lombok.extern.slf4j.Slf4j;

@PermitAll
@Slf4j
public abstract class OperationProviderImpl<DB extends org.jboss.pnc.model.Operation, DTO extends Operation>
        extends AbstractUpdatableProvider<Base32LongID, DB, DTO, OperationRef> implements OperationProvider<DB, DTO> {

    @Inject
    public OperationProviderImpl(
            Repository<DB, Base32LongID> repository,
            UpdatableEntityMapper<Base32LongID, DB, DTO, OperationRef> mapper,
            Class<DB> type) {
        super(repository, mapper, type);
    }

    @Override
    @DenyAll
    public DTO store(DTO restEntity) {
        throw new UnsupportedOperationException("Creating operations is prohibited!");
    }

    @Override
    @RolesAllowed(USERS_ADMIN)
    public DTO update(String operationId, DTO restEntity) {
        return super.update(operationId, restEntity);
    }

    @Override
    @DenyAll
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting operations is prohibited!");
    }

}
