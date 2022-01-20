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
import static org.jboss.pnc.spi.datastore.predicates.EnvironmentPredicates.withEnvironmentNameAndActive;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.facade.providers.api.EnvironmentProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
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
        return super.store(restEntity);
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting Environments is prohibited");
    }

    @Override
    protected void validateBeforeSaving(Environment restEntity) {

        super.validateBeforeSaving(restEntity);
        deprecateActiveEnvironmentsWithSameName(restEntity);
    }

    @SuppressWarnings("unchecked")
    private void deprecateActiveEnvironmentsWithSameName(Environment restEntity) throws ConflictedEntryException {

        List<BuildEnvironment> buildEnvironments = repository
                .queryWithPredicates(withEnvironmentNameAndActive(restEntity.getName()));

        for (BuildEnvironment env : buildEnvironments) {
            env.setDeprecated(true);
            repository.save(env);
        }
    }

}
