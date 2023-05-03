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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import static java.util.function.Function.identity;
import static org.jboss.pnc.api.constants.Defaults.GLOBAL_SCOPE;

import java.util.function.Function;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.AlignmentStrategy;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.mapper.api.AlignStratMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.EntityMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductReleaseMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.model.AlignStrategy;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
public class MapSetMapper {

    @Inject
    private BuildConfigurationMapper buildConfigurationMapper;

    @Inject
    private GroupConfigurationMapper groupConfigurationMapper;

    @Inject
    private ProductMilestoneMapper productMilestoneMapper;

    @Inject
    private ProductReleaseMapper productReleaseMapper;

    @Inject
    private ProductVersionMapper productVersionMapper;

    @Inject
    private AlignStratMapper alignStratMapper;

    @Inject
    private RefToReferenceMapper referenceMapper;

    public Set<BuildConfigurationSet> mapGC(Map<String, GroupConfigurationRef> value) {
        return map(value, groupConfigurationMapper, BuildConfigurationSet.class);
    }

    public Map<String, GroupConfigurationRef> mapGC(Collection<BuildConfigurationSet> value) {
        return map(value, groupConfigurationMapper);
    }

    public Set<BuildConfiguration> mapBC(Map<String, BuildConfigurationRef> value) {
        return map(value, buildConfigurationMapper, BuildConfiguration.class);
    }

    public Map<String, BuildConfigurationRef> mapBC(Collection<BuildConfiguration> value) {
        return map(value, buildConfigurationMapper);
    }

    public Set<ProductVersion> mapPV(Map<String, ProductVersionRef> value) {
        return map(value, productVersionMapper, ProductVersion.class);
    }

    public Map<String, ProductVersionRef> mapPV(Collection<ProductVersion> value) {
        return map(value, productVersionMapper);
    }

    public Set<ProductMilestone> mapPM(Map<String, ProductMilestoneRef> value) {
        return map(value, productMilestoneMapper, ProductMilestone.class);
    }

    public Map<String, ProductMilestoneRef> mapPM(Collection<ProductMilestone> value) {
        return map(value, productMilestoneMapper);
    }

    public Set<ProductRelease> mapPR(Map<String, ProductReleaseRef> value) {
        return map(value, productReleaseMapper, ProductRelease.class);
    }

    public Map<String, ProductReleaseRef> mapPR(Collection<ProductRelease> value) {
        return map(value, productReleaseMapper);
    }

    public Map<String, AlignStrategy> mapAC(Collection<AlignmentStrategy> value) {
        if (value == null) {
            return null;
        }
        return value.stream()
                .collect(
                        Collectors.toMap(
                                ac -> ac.getDependencyOverride() == null ? GLOBAL_SCOPE : ac.getDependencyOverride(),
                                alignStratMapper::toModel));
    }

    public Set<AlignmentStrategy> mapAC(Map<String, AlignStrategy> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(entry -> alignStratMapper.toDto(entry.getValue(), entry.getKey()))
                .collect(Collectors.toSet());
    }

    public Map<String, AlignStrategy> updateAlignStrats(
            Set<AlignmentStrategy> source,
            Map<String, AlignStrategy> target) {
        Map<String, AlignStrategy> oldASs = target;
        Map<String, AlignmentStrategy> newASs;
        if (source == null) {
            newASs = Collections.emptyMap();
        } else {
            newASs = source.stream()
                    .collect(
                            Collectors.toMap(
                                    ac -> ac.getDependencyOverride() == null ? GLOBAL_SCOPE
                                            : ac.getDependencyOverride(),
                                    Function.identity()));
        }

        CollectionMerger.merge(
                oldASs.keySet(),
                newASs.keySet(),
                add -> target.put(add, alignStratMapper.toModel(newASs.get(add))),
                remove -> target.remove(remove),
                update -> alignStratMapper.updateEntity(newASs.get(update), oldASs.get(update)));

        return target;
    }

    private <ID extends Serializable, DTO extends DTOEntity, DB extends GenericEntity<ID>> Set<DB> map(
            Map<String, DTO> value,
            EntityMapper<ID, DB, ?, DTO> mapper,
            Class<DB> targetClass) {
        if (value == null) {
            return null;
        }
        return value.values()
                .stream()
                .map(ref -> referenceMapper.map(ref, mapper.getIdMapper(), targetClass))
                .collect(Collectors.toSet());
    }

    private <DTO extends DTOEntity, DB extends GenericEntity<?>> Map<String, DTO> map(
            Collection<DB> value,
            EntityMapper<?, DB, ?, DTO> mapper) {
        if (value == null) {
            return null;
        }
        return value.stream().map(mapper::toRef).collect(Collectors.toMap(DTOEntity::getId, identity()));
    }
}
