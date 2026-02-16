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

import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.Attachment;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
        config = MapperCentralConfig.class,
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = { RefToReferenceMapper.class, BuildMapper.class })
public interface AttachmentMapper
        extends UpdatableEntityMapper<Integer, Attachment, org.jboss.pnc.dto.Attachment, AttachmentRef> {

    IntIdMapper idMapper = new IntIdMapper();

    @Override
    @Mapping(target = "build", source = "buildRecord")
    org.jboss.pnc.dto.Attachment toDTO(Attachment dbEntity);

    @Override
    @Reference
    @BeanMapping(ignoreUnmappedSourceProperties = { "buildRecord" })
    AttachmentRef toRef(Attachment dbEntity);

    @Override
    @Mapping(target = "buildRecord", source = "build", qualifiedBy = IdEntity.class)
    Attachment toEntity(org.jboss.pnc.dto.Attachment dtoEntity);

    @Named("avoid-ambiguous-mapping")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "build", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id", "buildRecord", "creationTime" })
    org.jboss.pnc.dto.Attachment toImportedDTO(Attachment dbEntity);

    @Override
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "md5", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "url", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "id", "md5", "creationTime", "url" })
    void updateEntity(org.jboss.pnc.dto.Attachment dtoEntity, @MappingTarget org.jboss.pnc.model.Attachment target);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return idMapper;
    }
}
