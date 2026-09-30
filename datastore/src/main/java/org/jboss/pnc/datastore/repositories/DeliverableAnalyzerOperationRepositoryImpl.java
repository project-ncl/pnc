/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;

@Stateless
public class DeliverableAnalyzerOperationRepositoryImpl
        extends AbstractRepository<DeliverableAnalyzerOperation, Base32LongID>
        implements DeliverableAnalyzerOperationRepository {

    public DeliverableAnalyzerOperationRepositoryImpl() {
        super(DeliverableAnalyzerOperation.class, Base32LongID.class);
    }
}
