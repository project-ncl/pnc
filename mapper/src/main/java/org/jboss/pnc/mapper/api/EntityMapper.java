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

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;

/**
 * Mappers that converts database entity to DTO entities and vice versa.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
public interface EntityMapper<ID extends Serializable, DB extends GenericEntity<ID>, DTO extends REF, REF extends DTOEntity> {

    /**
     * Converts DTO entity to database entity.
     * 
     * @param dtoEntity DTO entity to be converted.
     * @return Converted database entity.
     */
    DB toEntity(DTO dtoEntity);

    /**
     * Converts database entity to DTO entity.
     * 
     * @param dbEntity database entity to be converted.
     * @return Converted DTO entity.
     */
    DTO toDTO(DB dbEntity);

    /**
     * Converts database entity to DTO reference entity.
     *
     * @param dbEntity database entity to be converted.
     * @return Converted DTO reference entity.
     */
    REF toRef(DB dbEntity);

    IdMapper<ID, String> getIdMapper();
}
