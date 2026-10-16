/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import java.util.Objects;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

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

    public BuildConfigurationRevision findRevision(Integer id, org.jboss.pnc.dto.BuildConfiguration bcEntity) {
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
