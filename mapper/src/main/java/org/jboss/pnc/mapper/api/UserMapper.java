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

import org.jboss.pnc.mapper.IntIdMapper;
import org.jboss.pnc.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface UserMapper extends EntityMapper<Integer, User, org.jboss.pnc.dto.User, org.jboss.pnc.dto.User> {

    @Override
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "loginToken", ignore = true)
    @Mapping(target = "buildRecords", ignore = true)
    User toEntity(org.jboss.pnc.dto.User dtoEntity);

    @Override
    @Reference
    default org.jboss.pnc.dto.User toRef(User dbEntity) {
        return toDTO(dbEntity);
    }

    @Override
    @BeanMapping(ignoreUnmappedSourceProperties = { "email", "firstName", "lastName", "loginToken", "buildRecords" })
    org.jboss.pnc.dto.User toDTO(User dbEntity);

    @Override
    default IdMapper<Integer, String> getIdMapper() {
        return new IntIdMapper();
    }
}
