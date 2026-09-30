/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.datastore.repositories.OperationRepository;

@Stateless
public class OperationRepositoryImpl extends AbstractRepository<Operation, Base32LongID>
        implements OperationRepository {

    public OperationRepositoryImpl() {
        super(Operation.class, Base32LongID.class);
    }
}
