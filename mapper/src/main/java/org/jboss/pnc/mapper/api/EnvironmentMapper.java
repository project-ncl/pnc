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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.BuildEnvironment;
import org.mapstruct.Mapper;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface EnvironmentMapper extends EntityMapper<Integer, BuildEnvironment, Environment, Environment> {

    @Override
    BuildEnvironment toEntity(Environment dtoEntity);

    @Override
    @Reference
    default Environment toRef(BuildEnvironment dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    Environment toDTO(BuildEnvironment dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
