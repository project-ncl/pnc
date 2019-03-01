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
package org.jboss.pnc.facade.mapper.api;

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.model.Artifact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface ArtifactMapper extends EntityMapper<Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>{

    @Override
    org.jboss.pnc.dto.Artifact toDTO(Artifact dbEntity);

    @Override
    default Artifact toIDEntity(ArtifactRef dtoEntity) {
        return Artifact.Builder.newBuilder().id(dtoEntity.getId()).build();
    }

    @Override
    ArtifactRef toRef(Artifact dbEntity);
    
    @Override
    Artifact toEntity(org.jboss.pnc.dto.Artifact dtoEntity);
}
