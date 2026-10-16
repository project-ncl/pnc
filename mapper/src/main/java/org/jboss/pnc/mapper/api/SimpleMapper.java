/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
