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

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.EnvironmentPredicates.replacedBy;
import static org.jboss.pnc.spi.datastore.predicates.EnvironmentPredicates.withEnvironmentNameAndActive;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.api.constants.Attributes;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.facade.providers.api.EnvironmentProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;

@PermitAll
@Stateless
public class EnvironmentProviderImpl extends AbstractProvider<Integer, BuildEnvironment, Environment, Environment>
        implements EnvironmentProvider {

    @Inject
    public EnvironmentProviderImpl(BuildEnvironmentRepository repository, EnvironmentMapper mapper) {
        super(repository, mapper, BuildEnvironment.class);
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public Environment store(Environment restEntity) throws DTOValidationException {
        Environment storedEntity = super.store(restEntity);
        deprecateActiveEnvironmentsWithSameName(storedEntity);
        return storedEntity;
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting Environments is prohibited");
    }

    @Override
    protected void validateBeforeSaving(Environment restEntity) {
        super.validateBeforeSaving(restEntity);
        if (restEntity.isDeprecated()) {
            throw new InvalidEntityException("New environment cannot be created as deprecated.", "deprecated");
        }
        if (restEntity.getAttributes() != null
                && restEntity.getAttributes().containsKey(Attributes.DEPRECATION_REPLACEMENT)) {
            throw new InvalidEntityException(
                    "New environment cannot contain attribute '" + Attributes.DEPRECATION_REPLACEMENT + "'.",
                    "attributes");
        }
    }

    @SuppressWarnings("unchecked")
    private void deprecateActiveEnvironmentsWithSameName(Environment restEntity) throws ConflictedEntryException {
        List<BuildEnvironment> buildEnvironments = repository
                .queryWithPredicates(withEnvironmentNameAndActive(restEntity.getName()));

        for (BuildEnvironment env : buildEnvironments) {
            if (env.getId().equals(mapper.getIdMapper().toEntity(restEntity.getId()))) {
                continue;
            }
            env.setDeprecated(true);
            env.putAttribute(Attributes.DEPRECATION_REPLACEMENT, restEntity.getId());
            shortenTransitivePath(env.getId().toString(), restEntity.getId());
        }
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public Environment deprecateEnvironment(String id, String replacementId) {
        BuildEnvironment deprecatedEnvironment = repository.queryById(mapper.getIdMapper().toEntity(id));
        if (deprecatedEnvironment == null) {
            throw new EmptyEntityException("Environment with id " + id + " doesn't exist");
        }
        BuildEnvironment replacementEnvironment = repository.queryById(mapper.getIdMapper().toEntity(replacementId));
        if (replacementEnvironment == null) {
            throw new EmptyEntityException("Replacement environment with id " + replacementId + " doesn't exist");
        }
        if (replacementEnvironment.isDeprecated()) {
            throw new InvalidEntityException(
                    "Replacement environment with id " + replacementId + " is itself deprecated.");
        }
        deprecatedEnvironment.setDeprecated(true);
        deprecatedEnvironment.putAttribute(Attributes.DEPRECATION_REPLACEMENT, replacementId);
        shortenTransitivePath(id, replacementId);
        return mapper.toDTO(deprecatedEnvironment);
    }

    private void shortenTransitivePath(String deprecatedId, String replacementId) {
        for (BuildEnvironment environment : repository.queryWithPredicates(replacedBy(deprecatedId))) {
            environment.putAttribute(Attributes.DEPRECATION_REPLACEMENT, replacementId);
        }
    }
}
