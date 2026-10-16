/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

public interface IdMapper<EntityId, DtoId> {

    /**
     * Converts DTO ID to database entity ID.
     *
     * @param dtoId DTO ID to be converted.
     * @return Converted database entity ID.
     */
    EntityId toEntity(DtoId dtoId);

    /**
     * Converts database entity ID to DTO ID.
     *
     * @param entityId ID to be converted.
     * @return Converted DTO ID.
     */
    DtoId toDto(EntityId entityId);
}
