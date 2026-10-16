/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

/**
 * A base interface for all DTO entities to allow generic implementation of some standard operations
 */
public interface DTOEntity {

    /**
     * Get the entity Id
     *
     * @return Id
     */
    String getId();

}
