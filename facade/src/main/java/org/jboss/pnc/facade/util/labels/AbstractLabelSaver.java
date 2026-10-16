/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util.labels;

import java.io.Serializable;

import javax.transaction.Transactional;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.LabelEntryRepository;

/**
 * Concrete implementations of this class MUST BE annotated @RequestScoped.
 *
 * @param <LH_ID> The id of the label history entity.
 * @param <LH> The label history entity, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry}.
 */
public abstract class AbstractLabelSaver<LH_ID extends Serializable, LO_ID extends Serializable, L extends Enum<L>, LH extends GenericEntity<LH_ID>, LO extends GenericEntity<LO_ID>>
        implements LabelSaver<LO_ID, L, LO> {

    protected LO labeledObject;

    protected int nextChangeOrder;

    protected String reason;

    protected final LabelEntryRepository<LO_ID, LH_ID, LH> labelEntryRepository;

    public AbstractLabelSaver(LabelEntryRepository<LO_ID, LH_ID, LH> labelEntryRepository) {
        this.labelEntryRepository = labelEntryRepository;
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void init(LO labeledObject, String reason) {
        this.labeledObject = labeledObject;
        this.nextChangeOrder = labelEntryRepository.getLatestChangeOrderOfReport(labeledObject.getId());
        this.reason = reason;
    }
}
