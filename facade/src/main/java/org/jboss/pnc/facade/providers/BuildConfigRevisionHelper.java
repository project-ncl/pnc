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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateBuildConfiguration(org.jboss.pnc.model.BuildConfiguration bcEntity) {
        buildConfigurationRepository.save(bcEntity);
    }

    public BuildConfigurationRevision findRevision(Integer id, BuildConfiguration bcEntity) {
        return buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(id)
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

    public static boolean equalValues(BuildConfigurationAudited audited, BuildConfiguration query) {
        return audited.getName().equals(query.getName())
                && Objects.equals(audited.getBuildScript(), query.getBuildScript())
                && equalsId(audited.getRepositoryConfiguration(), query.getRepositoryConfiguration())
                && Objects.equals(audited.getScmRevision(), query.getScmRevision())
                && equalsId(audited.getProject(), query.getProject())
                && equalsId(audited.getBuildEnvironment(), query.getBuildEnvironment())
                && audited.getGenericParameters().equals(query.getGenericParameters());
    }

    private static boolean equalsId(GenericEntity<Integer> dbEntity, GenericEntity<Integer> query) {
        if (dbEntity == null || query == null) {
            return dbEntity == query;
        }

        return dbEntity.getId().equals(query.getId());
    }

}
