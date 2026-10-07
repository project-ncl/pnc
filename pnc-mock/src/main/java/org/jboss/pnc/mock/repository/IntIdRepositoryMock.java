/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.pnc.model.GenericEntity;

public abstract class IntIdRepositoryMock<EntityType extends GenericEntity<Integer>>
        extends RepositoryMock<Integer, EntityType> {

    public static final AtomicInteger idSequence = new AtomicInteger();

    @Override
    public Integer getNextId() {
        return idSequence.getAndIncrement();
    }
}
