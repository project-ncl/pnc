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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Mapper(config = MapperCentralConfig.class, uses = {BuildMapper.IDMapper.class})
public interface BuildPushResultMapper {

    @Mapping(target = "buildId", source = "buildRecord")
    @Mapping(target = "artifactImportErrors", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"tagPrefix"})
    BuildPushResult toDTO(BuildRecordPushResult db);

    @Mapping(target = "buildRecord", source = "buildId")
    @Mapping(target = "tagPrefix", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"artifactImportErrors"})
    BuildRecordPushResult toEntity(BuildPushResult dto);
}
