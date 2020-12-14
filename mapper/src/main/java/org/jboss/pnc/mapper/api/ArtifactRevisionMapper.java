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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.ArtifactRevisionRef;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.ArtifactAudited;
import org.jboss.pnc.model.IdRev;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Andrea Vibelli &lt;avibelli@redhat.com&gt;
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { UserMapper.class, RefToReferenceMapper.class },
        imports = IdRev.class)
public interface ArtifactRevisionMapper {

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "modificationUser", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "idRev", "artifact" })
    ArtifactRevision toDTO(ArtifactAudited dbEntity);

    @Mapping(
            target = "idRev",
            expression = "java( new IdRev( Integer.valueOf(dtoEntity.getId()), dtoEntity.getRev() ) )")
    @Mapping(target = "artifact", ignore = true)
    @Mapping(target = "modificationUser", qualifiedBy = IdEntity.class)
    ArtifactAudited toEntity(ArtifactRevision dtoEntity);

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @BeanMapping(ignoreUnmappedSourceProperties = { "idRev", "artifact", "creationUser", "modificationUser" })
    ArtifactRevisionRef toRef(ArtifactAudited dbEntity);

}
