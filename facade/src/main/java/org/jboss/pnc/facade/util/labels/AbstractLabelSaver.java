/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.util.labels;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.LabelEntryRepository;

import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * Concrete implementations of this class MUST BE annotated @RequestScoped.
 */
public abstract class AbstractLabelSaver<LH_ID extends Serializable, LO_ID extends Serializable, L extends Enum<L>, LH extends GenericEntity<LH_ID>, LO extends GenericEntity<LO_ID>> implements LabelSaver<LH_ID, LO_ID, L, LH, LO> {

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
