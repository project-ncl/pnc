/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link Operation} entity
 */
public interface OperationRepository extends Repository<Operation, Base32LongID> {

}
