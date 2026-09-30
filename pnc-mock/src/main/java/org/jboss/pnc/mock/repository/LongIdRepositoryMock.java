/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.model.GenericEntity;

public abstract class LongIdRepositoryMock<EntityType extends GenericEntity<Long>>
        extends RepositoryMock<Long, EntityType> {

    @Override
    public Long getNextId() {
        return Sequence.nextId();
    }
}
