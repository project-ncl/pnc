/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util.labels;

import java.util.EnumSet;

import javax.transaction.Transactional;

import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;

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
