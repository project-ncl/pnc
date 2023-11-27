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

import javax.transaction.Transactional;
import java.util.EnumSet;

/**
 * Concrete implementations of this class has to be annotated {@value @RequestScoped}.
 *
 * @param <L> label entity, e.g. {@link org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel}
 */
public abstract class AbstractLabelModifier<L extends Enum<L>> implements LabelModifier<L> {

    private EnumSet<L> activeLabels;

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void validateAndAddLabel(L label, EnumSet<L> activeLabels) {
        this.activeLabels = activeLabels;
        checkLabelIsNotPresent(label);
        addLabel(label, activeLabels);
    }

    protected abstract void addLabel(L label, EnumSet<L> activeLabels);

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void validateAndRemoveLabel(L label, EnumSet<L> activeLabels) {
        this.activeLabels = activeLabels;
        checkLabelIsPresent(label);
        removeLabel(label, activeLabels);
    }

    protected abstract void removeLabel(L label, EnumSet<L> activeLabels);

    private void checkLabelIsNotPresent(L label) {
        if (activeLabels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    activeLabels,
                    LabelOperation.ADDED,
                    "label already present in the set of active labels");
        }
    }

    private void checkLabelIsPresent(L label) {
        if (!activeLabels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    activeLabels,
                    LabelOperation.REMOVED,
                    "no such label present in the set of active labels");
        }
    }
}
