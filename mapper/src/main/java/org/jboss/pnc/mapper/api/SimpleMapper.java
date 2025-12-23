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

/**
 * Mappers that converts an internal MODEL entity to DTO entities and vice versa.
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 * @param <DTO> The external DTO entity type
 * @param <MODEL> The internal MODEL entity type
 */
public interface SimpleMapper<DTO, MODEL> {

    /**
     * Converts DTO entity to internal entity.
     *
     * @param dto DTO entity to be converted.
     * @return Converted internal entity.
     */
    MODEL toEntity(DTO dto);

    /**
     * Converts internal entity to DTO entity.
     *
     * @param entity internal entity to be converted.
     * @return Converted DTO entity.
     */
    DTO toDTO(MODEL entity);
}
