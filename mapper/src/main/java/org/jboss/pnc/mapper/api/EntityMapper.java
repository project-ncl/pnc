/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import java.io.Serializable;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.model.GenericEntity;

/**
 * Mappers that converts database entity to DTO entities and vice versa.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <ID> The type of the entity identifier
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
public interface EntityMapper<ID extends Serializable, DB extends GenericEntity<ID>, DTO extends REF, REF extends DTOEntity> {

    /**
     * Converts DTO entity to database entity. This should be used only when creating new entity.
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
