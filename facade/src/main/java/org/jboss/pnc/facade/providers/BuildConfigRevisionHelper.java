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

import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.Objects;

import javax.transaction.Transactional;

@PermitAll
@Stateless
public class BuildConfigRevisionHelper {

    private final Logger logger = LoggerFactory.getLogger(BuildConfigRevisionHelper.class);

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    /**
     * Updates the Build Config in new transaction. This is necessary when you want to get the newly create Build Config
     * revision, as Envers creates new revisions when transaction commits.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateBuildConfiguration(String id, org.jboss.pnc.dto.BuildConfiguration bcEntity) {
        buildConfigurationProvider.update(id, bcEntity);
    }

    public BuildConfigurationRevision findRevision(String id, org.jboss.pnc.dto.BuildConfiguration bcEntity) {
        return buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(Integer.valueOf(id))
                .stream()
                .peek(p -> logger.warn("going through: " + p))
                .filter(bca -> equalValues(bca, bcEntity))
                .findFirst()
                .map(buildConfigurationRevisionMapper::toDTO)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Couldn't find updated BuildConfigurationAudited entity. "
                                        + "BuildConfiguration to be stored: " + bcEntity));
    }

    public static boolean equalValues(BuildConfigurationAudited persisted, org.jboss.pnc.dto.BuildConfiguration query) {
        return Objects.equals(persisted.getName(), query.getName())
                && Objects.equals(persisted.getBuildScript(), query.getBuildScript())
                && equalsId(persisted.getRepositoryConfiguration(), query.getScmRepository())
                && Objects.equals(persisted.getScmRevision(), query.getScmRevision())
                && equalsId(persisted.getProject(), query.getProject())
                && equalsId(persisted.getBuildEnvironment(), query.getEnvironment())
                && Objects.equals(persisted.getGenericParameters(), query.getParameters())
                && (persisted.getBuildType() == query.getBuildType());
    }

    public static boolean equalValues(BuildConfiguration persisted, org.jboss.pnc.dto.BuildConfiguration query) {
        return Objects.equals(persisted.getName(), query.getName())
                && Objects.equals(persisted.getBuildScript(), query.getBuildScript())
                && equalsId(persisted.getRepositoryConfiguration(), query.getScmRepository())
                && Objects.equals(persisted.getScmRevision(), query.getScmRevision())
                && equalsId(persisted.getProject(), query.getProject())
                && equalsId(persisted.getBuildEnvironment(), query.getEnvironment())
                && Objects.equals(persisted.getGenericParameters(), query.getParameters())
                && (persisted.getBuildType() == query.getBuildType());
    }

    private static boolean equalsId(GenericEntity<Integer> persisted, DTOEntity toUpdate) {
        if (persisted == null && toUpdate == null) {
            return true;
        }
        if (persisted == null || toUpdate == null) {
            return false;
        }
        return persisted.getId().toString().equals(toUpdate.getId());
    }
}
