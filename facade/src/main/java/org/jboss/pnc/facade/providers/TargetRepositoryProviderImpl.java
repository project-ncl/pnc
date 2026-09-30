/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ARTIFACT_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_BUILD_ADMIN;
import static org.jboss.pnc.spi.datastore.predicates.TargetRepositoryPredicates.withIdentifierAndPathIn;

import java.util.Collections;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.facade.providers.api.TargetRepositoryProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.model.TargetRepository.IdentifierPath;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;

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
    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ARTIFACT_ADMIN, USERS_ADMIN })
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
