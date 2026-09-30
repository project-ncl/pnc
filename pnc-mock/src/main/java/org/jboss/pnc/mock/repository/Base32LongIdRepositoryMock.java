/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.GenericEntity;

public abstract class Base32LongIdRepositoryMock<EntityType extends GenericEntity<Base32LongID>>
        extends RepositoryMock<Base32LongID, EntityType> {

    @Override
    public Base32LongID getNextId() {
        return new Base32LongID(Sequence.nextId());
    }
}
