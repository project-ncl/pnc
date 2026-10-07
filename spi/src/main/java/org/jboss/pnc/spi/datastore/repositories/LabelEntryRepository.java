/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.io.Serializable;

import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 *
 * @param <LO_ID>, e.g. id of {@link DeliverableAnalyzerReport}
 * @param <LH_ID>
 * @param <LH>, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry}
 */
public interface LabelEntryRepository<LO_ID extends Serializable, LH_ID extends Serializable, LH extends GenericEntity<LH_ID>>
        extends Repository<LH, LH_ID> {

    /**
     * Gets the latest changeOrder for the labeled object, given by its id
     *
     * @param labeledObjectId the ID of the labeled object
     * @return latest changeOrder
     */
    Integer getLatestChangeOrderOfReport(LO_ID labeledObjectId);
}
