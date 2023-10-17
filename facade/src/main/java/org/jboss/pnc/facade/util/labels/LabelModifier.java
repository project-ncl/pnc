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

import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;
import org.jboss.pnc.model.GenericEntity;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * The class implementing this interface is able to add (remove) new (old) label to (from) the set of active labels and
 * update the label history. Such a class complies with the rules of adding (removing) label for the given entity type
 * E. Concrete implementations of this class has to be annotated {@value @RequestScoped}.
 *
 * @param <L> label entity, e.g. {@link org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel}
 * @param <LH> label history entity, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry}
 * @param <LH_ID> the ID type of the label history entity
 * @param <LO_ID> the ID type of the labeled object entity, which is e.g.
 *        {@link org.jboss.pnc.model.DeliverableAnalyzerReport}
 */
public abstract class LabelModifier<LO_ID extends Serializable, LH_ID extends Serializable, L extends Enum<L>, LH extends GenericEntity<LH_ID>> {

    protected LO_ID labeledObjectId;

    protected EnumSet<L> activeLabels;

    protected String reason;

    @Transactional(Transactional.TxType.MANDATORY)
    public void addLabel(
            LO_ID labeledObjectId,
            L label,
            EnumSet<L> activeLabels,
            String reason) {
        this.labeledObjectId = labeledObjectId;
        this.activeLabels = activeLabels;
        this.reason = reason;
        validateAndAdd(label);
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void removeLabel(
            LO_ID labeledObjectId,
            L label,
            EnumSet<L> activeLabels,
            String reason) {
        this.labeledObjectId = labeledObjectId;
        this.activeLabels = activeLabels;
        this.reason = reason;
        validateAndRemove(label);
    }

    protected abstract void validateAndAdd(L label);

    protected abstract void validateAndRemove(L label);

    protected void checkLabelIsNotPresent(L label) {
        if (activeLabels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    activeLabels,
                    LabelOperation.ADDED,
                    "label already present in the set of active labels");
        }
    }

    protected void checkLabelIsPresent(L label) {
        if (!activeLabels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    activeLabels,
                    LabelOperation.REMOVED,
                    "no such label present in the set of active labels");
        }
    }
}
