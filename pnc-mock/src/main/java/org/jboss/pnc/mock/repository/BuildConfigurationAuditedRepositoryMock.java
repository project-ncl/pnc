/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:25 PM
 */
public class BuildConfigurationAuditedRepositoryMock implements BuildConfigurationAuditedRepository {

    private final AtomicInteger idSequence = new AtomicInteger(0);
    protected final List<BuildConfigurationAudited> data = new ArrayList<>();

    public BuildConfigurationAudited save(BuildConfigurationAudited entity) {
        IdRev idRev = entity.getIdRev();

        if (idRev == null) {
            throw new IllegalStateException(
                    "auto-setting " + this.getClass().getSimpleName() + " entity id is not supported");
        }

        BuildConfiguration buildConfiguration = entity.getBuildConfiguration();

        Integer newRev = idSequence.getAndIncrement();
        BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, newRev);

        getOptionalById(idRev).ifPresent(data::remove);
        data.add(entity);
        return entity;
    }

    public BuildConfigurationAudited findLatestById(int buildConfigurationId) {
        return data.stream()
                .filter(c -> c.getId().equals(buildConfigurationId))
                .sorted((c1, c2) -> c2.getRev().compareTo(c1.getRev()))
                .findFirst()
                .orElse(null);
    }

    public List<BuildConfigurationAudited> queryAll() {
        return data;
    }

    public void delete(IdRev id) {
        data.removeIf(c -> c.getId().equals(id.getId()));
    }

    @Override
    public List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id) {
        return data.stream()
                .filter(c -> c.getId().equals(id))
                .sorted((c1, c2) -> c2.getRev().compareTo(c1.getRev()))
                .collect(Collectors.toList());
    }

    private Optional<BuildConfigurationAudited> getOptionalById(IdRev id) {
        return data.stream().filter(m -> id.getId().equals(m.getId())).findAny();
    }

    public BuildConfigurationAudited queryById(IdRev id) {
        return getOptionalById(id).orElseThrow(() -> new RuntimeException("Didn't find entity for id: " + id));
    }

    public Map<IdRev, BuildConfigurationAudited> queryById(Set<IdRev> idRevs) {

        return idRevs.stream()
                .map(
                        idRev -> getOptionalById(idRev)
                                .orElseThrow(() -> new RuntimeException("Didn't find entity for id: " + idRev)))
                .collect(Collectors.toMap(BuildConfigurationAudited::getIdRev, bca -> bca));
    }

    @Override
    public List<BuildConfigurationAudited> searchForBuildConfigurationName(String buildConfigurationName) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<IdRev> searchIdRevForBuildConfigurationName(String buildConfigurationName) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<IdRev> searchIdRevForBuildConfigurationNameOrProjectName(
            List<Project> projectsMatchingName,
            String name) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<IdRev> searchIdRevForProjectId(Integer projectId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
