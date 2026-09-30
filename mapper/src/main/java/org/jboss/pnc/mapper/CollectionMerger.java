/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
public class CollectionMerger {

    private MapSetMapper mapSetMapper;

    // CDI
    public CollectionMerger() {
    }

    @Inject
    public CollectionMerger(MapSetMapper mapSetMapper) {
        this.mapSetMapper = mapSetMapper;
    }

    public <C> void merge(Collection<C> oldContent, Collection<C> newContent, Consumer<C> adder, Consumer<C> remover) {
        if (newContent == null) {
            newContent = Collections.emptyList();
        }
        Set<C> toRemove = new HashSet<>(oldContent);
        toRemove.removeAll(newContent);
        toRemove.forEach(remover);

        Set<C> toAdd = new HashSet<>(newContent);
        toAdd.removeAll(oldContent);
        toAdd.forEach(adder);
    }

    public Set<BuildConfiguration> updateDependencies(
            org.jboss.pnc.dto.BuildConfiguration dto,
            BuildConfiguration target) {
        Set<BuildConfiguration> oldBCs = target.getDependencies();
        Set<BuildConfiguration> newBCs = mapSetMapper.mapBC(dto.getDependencies());
        merge(oldBCs, newBCs, target::addDependency, target::removeDependency);
        return target.getDependencies();
    }

    public Set<BuildConfigurationSet> updateGroupConfigs(
            org.jboss.pnc.dto.BuildConfiguration dto,
            BuildConfiguration target) {
        Set<BuildConfigurationSet> oldGCs = target.getBuildConfigurationSets();
        Set<BuildConfigurationSet> newGCs = mapSetMapper.mapGC(dto.getGroupConfigs());
        merge(oldGCs, newGCs, target::addBuildConfigurationSet, target::removeBuildConfigurationSet);
        return target.getBuildConfigurationSets();
    }

    public Set<BuildConfiguration> updateBuildConfigs(GroupConfiguration dto, BuildConfigurationSet target) {
        Set<BuildConfiguration> oldBCs = target.getBuildConfigurations();
        Set<BuildConfiguration> newBCs = mapSetMapper.mapBC(dto.getBuildConfigs());
        merge(oldBCs, newBCs, target::addBuildConfiguration, target::removeBuildConfiguration);
        return target.getBuildConfigurations();
    }

    public Set<BuildConfiguration> updateBuildConfigs(org.jboss.pnc.dto.ProductVersion dto, ProductVersion target) {
        Set<BuildConfiguration> oldBCs = target.getBuildConfigurations();
        Set<BuildConfiguration> newBCs = mapSetMapper.mapBC(dto.getBuildConfigs());
        merge(oldBCs, newBCs, target::addBuildConfiguration, target::removeBuildConfiguration);
        return target.getBuildConfigurations();
    }

    public Set<BuildConfigurationSet> updateGroupConfigs(org.jboss.pnc.dto.ProductVersion dto, ProductVersion target) {
        Set<BuildConfigurationSet> oldGCs = target.getBuildConfigurationSets();
        Set<BuildConfigurationSet> newGCs = mapSetMapper.mapGC(dto.getGroupConfigs());
        merge(oldGCs, newGCs, target::addBuildConfigurationSet, target::removeBuildConfigurationSet);
        return target.getBuildConfigurationSets();
    }

}
