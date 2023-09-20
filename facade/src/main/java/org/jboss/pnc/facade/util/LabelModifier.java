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
package org.jboss.pnc.facade.util;

import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * The class implementing this interface is able to add (remove) new (old) label to (from) the set of active labels and
 * update the label history. Such a class complies with the rules of adding (removing) label for the given entity type
 * E.
 *
 * @param <L> label entity, e.g. {@link org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel}
 * @param <LH> label history entity, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry}
 * @param <LH_ID> the ID type of the label history entity
 * @param <LO_ID> the ID type of the labeled object entity, which is e.g.
 *        {@link org.jboss.pnc.model.DeliverableAnalyzerReport}
 */
public abstract class LabelModifier<LO_ID extends Serializable, LH_ID extends Serializable, L extends Enum<L>, LH extends GenericEntity<LH_ID>> {

    @Inject
    protected Repository<LH, LH_ID> labelHistoryRepository;

    public abstract void addLabelToActiveLabels(LO_ID labeledObjectId, L label, EnumSet<L> activeLabels)
            throws InvalidLabelOperationException;

    public abstract void removeLabelFromActiveLabels(LO_ID labeledObjectId, L label, EnumSet<L> activeLabels)
            throws InvalidLabelOperationException;

    @Transactional
    public void addLabelToActiveLabelsAndModifyLabelHistory(
            LO_ID labeledObjectId,
            L label,
            EnumSet<L> activeLabels,
            LH labelHistoryEntry) {
        addLabelToActiveLabels(labeledObjectId, label, activeLabels);
        modifyLabelHistory(labelHistoryEntry);
    }

    @Transactional
    public void removeLabelFromActiveLabelsAndModifyLabelHistory(
            LO_ID labeledObjectId,
            L label,
            EnumSet<L> activeLabels,
            LH labelHistoryEntry) {
        removeLabelFromActiveLabels(labeledObjectId, label, activeLabels);
        modifyLabelHistory(labelHistoryEntry);
    }

    private void modifyLabelHistory(LH labelHistoryEntry) {
        labelHistoryRepository.save(labelHistoryEntry);
    }

    protected void checkLabelIsNotPresent(L label, EnumSet<L> labels) {
        if (labels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    labels,
                    LabelOperation.ADDED,
                    "label already present in the set of active labels");
        }
    }

    protected void checkLabelIsPresent(L label, EnumSet<L> labels) {
        if (!labels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    labels,
                    LabelOperation.REMOVED,
                    "no such label present in the set of active labels");
        }
    }
}
