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

    @Inject
    private MapSetMapper mapSetMapper;

    public static <C> void merge(
            Collection<C> oldContent,
            Collection<C> newContent,
            Consumer<C> adder,
            Consumer<C> remover) {
        merge(oldContent, newContent, adder, remover, (ign) -> {});
    }

    public static <C> void merge(
            Collection<C> oldContent,
            Collection<C> newContent,
            Consumer<C> adder,
            Consumer<C> remover,
            Consumer<C> updater) {
        if (newContent == null) {
            newContent = Collections.emptyList();
        }
        Set<C> toRemove = new HashSet<>(oldContent);
        toRemove.removeAll(newContent);
        toRemove.forEach(remover);

        Set<C> toAdd = new HashSet<>(newContent);
        toAdd.removeAll(oldContent);
        toAdd.forEach(adder);

        Set<C> toUpdate = new HashSet<>(oldContent);
        toUpdate.retainAll(newContent);
        toUpdate.forEach(updater);
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
