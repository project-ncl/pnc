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

import org.jboss.pnc.api.orch.dto.RepositoryManagerResultRest;
import org.jboss.pnc.spi.repositorymanager.DefaultRepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class, uses = { ArtifactMapper.class })
public interface RepositoryManagerResultMapper
        extends SimpleMapper<RepositoryManagerResultRest, RepositoryManagerResult> {

    @Override
    @Mapping(target = "builtArtifacts", source = "builtArtifacts", qualifiedBy = TransientTargetRepo.class)
    @Mapping(target = "dependencies", source = "dependencies", qualifiedBy = TransientTargetRepo.class)
    @BeanMapping(resultType = DefaultRepositoryManagerResult.class)
    RepositoryManagerResult toEntity(RepositoryManagerResultRest repositoryManagerResultRest);

    @Override
    RepositoryManagerResultRest toDTO(RepositoryManagerResult entity);
}
