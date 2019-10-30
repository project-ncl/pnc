/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import java.util.Map;
import java.util.Set;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.EntityMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.GenericEntity;

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


    public Set<BuildConfigurationSet> mapGC(Map<String, GroupConfigurationRef> value) {
        return map(value, groupConfigurationMapper);
    }

    public Map<String, GroupConfigurationRef> mapGC(Collection<BuildConfigurationSet> value) {
        return map(value, groupConfigurationMapper);
    }

    public Set<BuildConfiguration> mapBC(Map<String, BuildConfigurationRef> value) {
        return map(value, buildConfigurationMapper);
    }

    public Map<String, BuildConfigurationRef> mapBC(Collection<BuildConfiguration> value) {
        return map(value, buildConfigurationMapper);
    }

    private <DTO extends DTOEntity, DB extends GenericEntity<?>> Set<DB> map(Map<String, DTO> value, EntityMapper<?, DB, ?, DTO> mapper) {
        if (value == null) {
            return null;
        }
        return value.values().stream()
                .map(mapper::toIDEntity)
                .collect(Collectors.toSet());
    }

    private <DTO extends DTOEntity, DB extends GenericEntity<?>> Map<String, DTO> map(Collection<DB> value, EntityMapper<?, DB, ?, DTO> mapper) {
        if (value == null) {
            return null;
        }
        return value.stream()
                .map(mapper::toRef)
                .collect(Collectors.toMap(DTOEntity::getId, identity()));
    }
}
