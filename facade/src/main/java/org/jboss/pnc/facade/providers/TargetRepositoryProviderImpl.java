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

import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.facade.providers.api.TargetRepositoryProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.model.TargetRepository.IdentifierPath;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.Collections;
import java.util.Set;

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.TargetRepositoryPredicates.withIdentifierAndPathIn;

@PermitAll
@Stateless
public class TargetRepositoryProviderImpl
        extends AbstractProvider<Integer, org.jboss.pnc.model.TargetRepository, TargetRepository, TargetRepository>
        implements TargetRepositoryProvider {

    @Inject
    public TargetRepositoryProviderImpl(TargetRepositoryRepository repository, TargetRepositoryMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.TargetRepository.class);
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public TargetRepository store(TargetRepository restEntity) throws DTOValidationException {
        return super.store(restEntity);
    }

    @Override
    protected void validateBeforeSaving(TargetRepository projectRest) {

        super.validateBeforeSaving(projectRest);
        validateIfNotConflicted(projectRest);
    }

    /**
     * Not allowed to delete a project
     *
     * @param id
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting target repositories is prohibited!");
    }

    @SuppressWarnings("unchecked")
    private void validateIfNotConflicted(TargetRepository targetRepositoryRest) throws ConflictedEntryException {

        Set<IdentifierPath> identifierAndPath = Collections.singleton(
                new IdentifierPath(targetRepositoryRest.getIdentifier(), targetRepositoryRest.getRepositoryPath()));
        org.jboss.pnc.model.TargetRepository targetRepository = repository
                .queryByPredicates(withIdentifierAndPathIn(identifierAndPath));

        Integer targetRepositoryId = null;

        if (targetRepositoryRest.getId() != null) {
            targetRepositoryId = Integer.valueOf(targetRepositoryRest.getId());
        }

        // don't validate against myself
        if (targetRepository != null && !targetRepository.getId().equals(targetRepositoryId)) {

            throw new ConflictedEntryException(
                    "Target Repository of that identifier and path already exists",
                    org.jboss.pnc.model.TargetRepository.class,
                    targetRepository.getIdentifierPath().toString());
        }
    }
}
