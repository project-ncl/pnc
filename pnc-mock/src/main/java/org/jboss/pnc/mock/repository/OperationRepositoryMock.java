/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.datastore.repositories.OperationRepository;

public class OperationRepositoryMock extends Base32LongIdRepositoryMock<Operation> implements OperationRepository {

}
