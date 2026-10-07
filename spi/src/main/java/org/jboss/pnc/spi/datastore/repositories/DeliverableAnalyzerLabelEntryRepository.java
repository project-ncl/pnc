/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;

/**
 * Interface for manipulating {@link DeliverableAnalyzerLabelEntry} entity
 */
public interface DeliverableAnalyzerLabelEntryRepository
        extends LabelEntryRepository<Base32LongID, Base32LongID, DeliverableAnalyzerLabelEntry> {

}
