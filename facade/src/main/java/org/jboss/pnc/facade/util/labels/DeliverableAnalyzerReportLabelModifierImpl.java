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

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.EnumSet;

/**
 * {@link LabelModifier} for {@link DeliverableAnalyzerReportLabel} entity.
 */
@RequestScoped
public class DeliverableAnalyzerReportLabelModifierImpl extends LabelModifier<DeliverableAnalyzerReportLabel>
        implements DeliverableAnalyzerReportLabelModifier {

    private final DeliverableAnalyzerLabelSaver deliverableAnalyzerLabelSaver;

    public static final String ERR_DELETED_ADD_RELEASED = "cannot mark as RELEASED the report which is already marked DELETED";
    public static final String ERR_SCRATCH_ADD_RELEASED = "cannot mark as RELEASED the report which is already marked SCRATCH";
    public static final String ERR_ADD_SCRATCH = "label can be marked as SCRATCH only when the analysis is executed";
    public static final String ERR_REMOVE_SCRATCH = "label marked SCRATCH cannot be removed";

    @Inject
    public DeliverableAnalyzerReportLabelModifierImpl(DeliverableAnalyzerLabelSaver deliverableAnalyzerLabelSaver) {
        this.deliverableAnalyzerLabelSaver = deliverableAnalyzerLabelSaver;
    }

    @Override
    public void addLabel(DeliverableAnalyzerReportLabel label, EnumSet<DeliverableAnalyzerReportLabel> activeLabels)
            throws InvalidLabelOperationException {
        switch (label) {
            case DELETED:
                if (activeLabels.contains(DeliverableAnalyzerReportLabel.RELEASED)) {
                    deliverableAnalyzerLabelSaver.removeLabel(DeliverableAnalyzerReportLabel.RELEASED);
                }
                deliverableAnalyzerLabelSaver.addLabel(label);
                break;
            case RELEASED:
                if (activeLabels.contains(DeliverableAnalyzerReportLabel.DELETED)) {
                    invalid(label, activeLabels, LabelOperation.ADDED, ERR_DELETED_ADD_RELEASED);
                } else if (activeLabels.contains(DeliverableAnalyzerReportLabel.SCRATCH)) {
                    invalid(label, activeLabels, LabelOperation.ADDED, ERR_SCRATCH_ADD_RELEASED);
                }
                deliverableAnalyzerLabelSaver.addLabel(label);
                break;
            case SCRATCH:
                invalid(label, activeLabels, LabelOperation.ADDED, ERR_ADD_SCRATCH);
            default:
                throw new UnsupportedOperationException("Adding of label " + label + " is not supported");
        }
    }

    @Override
    public void removeLabel(
            DeliverableAnalyzerReportLabel label,
            EnumSet<DeliverableAnalyzerReportLabel> activeLabels) {
        switch (label) {
            case SCRATCH:
                throw new InvalidLabelOperationException(
                        label,
                        activeLabels,
                        LabelOperation.REMOVED,
                        ERR_REMOVE_SCRATCH);
            case DELETED:
            case RELEASED:
                deliverableAnalyzerLabelSaver.removeLabel(label);
                break;
            default:
                throw new UnsupportedOperationException("Deleting of label " + label + " is not supported");
        }
    }

    private static void invalid(
            DeliverableAnalyzerReportLabel label,
            EnumSet<DeliverableAnalyzerReportLabel> labels,
            LabelOperation labelOperation,
            String reason) {
        throw new InvalidLabelOperationException(label, labels, labelOperation, reason);
    }
}