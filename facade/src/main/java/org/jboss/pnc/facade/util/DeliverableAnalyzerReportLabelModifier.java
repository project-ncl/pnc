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

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.EnumSet;

/**
 * {@link LabelModifier} for {@link DeliverableAnalyzerReportLabel} entity.
 */
@ApplicationScoped
public class DeliverableAnalyzerReportLabelModifier extends
        LabelModifier<Base32LongID, Base32LongID, DeliverableAnalyzerReportLabel, DeliverableAnalyzerLabelEntry> {

    @Inject
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    @Inject
    private DeliverableAnalyzerLabelEntryRepository deliverableAnalyzerLabelEntryRepository;

    public static final String ERR_DELETED_ADD_RELEASED = "cannot mark as RELEASED the report which is already marked DELETED";
    public static final String ERR_SCRATCH_ADD_RELEASED = "cannot mark as RELEASED the report which is already marked SCRATCH";
    public static final String ERR_ADD_SCRATCH = "label can be marked as SCRATCH only when the analysis is executed";
    public static final String ERR_REMOVE_SCRATCH = "label marked SCRATCH cannot be removed";

    @Inject
    public DeliverableAnalyzerReportLabelModifier() {
        super();

        super.labelHistoryRepository = deliverableAnalyzerLabelEntryRepository;
    }

    @Override
    public void addLabelToActiveLabels(
            Base32LongID reportId,
            DeliverableAnalyzerReportLabel label,
            EnumSet<DeliverableAnalyzerReportLabel> activeLabels) throws InvalidLabelOperationException {
        checkLabelIsNotPresent(label, activeLabels);

        switch (label) {
            case DELETED:
                if (activeLabels.contains(DeliverableAnalyzerReportLabel.RELEASED)) {
                    activeLabels.remove(DeliverableAnalyzerReportLabel.RELEASED);
                }
                activeLabels.add(label);
                break;
            case RELEASED:
                if (activeLabels.contains(DeliverableAnalyzerReportLabel.DELETED)) {
                    invalid(label, activeLabels, LabelOperation.ADDED, ERR_DELETED_ADD_RELEASED);
                } else if (activeLabels.contains(DeliverableAnalyzerReportLabel.SCRATCH)) {
                    invalid(label, activeLabels, LabelOperation.ADDED, ERR_SCRATCH_ADD_RELEASED);
                }
                activeLabels.add(label);
                break;
            case SCRATCH:
                invalid(label, activeLabels, LabelOperation.ADDED, ERR_ADD_SCRATCH);
            default:
                throw new UnsupportedOperationException("Adding of label " + label + " is not supported");
        }

        saveActiveLabels(reportId, activeLabels);
    }

    @Override
    public void removeLabelFromActiveLabels(
            Base32LongID reportId,
            DeliverableAnalyzerReportLabel label,
            EnumSet<DeliverableAnalyzerReportLabel> activeLabels) throws InvalidLabelOperationException {
        checkLabelIsPresent(label, activeLabels);

        switch (label) {
            case SCRATCH:
                if (activeLabels.contains(label)) {
                    throw new InvalidLabelOperationException(label, activeLabels, LabelOperation.REMOVED, ERR_REMOVE_SCRATCH);
                }
                // Do not break!! We want the SCRATCH to be removed in this case
            case DELETED:
            case RELEASED:
                activeLabels.remove(label);
                break;
            default:
                throw new UnsupportedOperationException("Deleting of label " + label + " is not supported");
        }

        saveActiveLabels(reportId, activeLabels);
    }

    private void saveActiveLabels(Base32LongID reportId, EnumSet<DeliverableAnalyzerReportLabel> activeLabels) {
        DeliverableAnalyzerReport report = deliverableAnalyzerReportRepository.queryById(reportId);
        report.setLabels(activeLabels);
        deliverableAnalyzerReportRepository.save(report);
    }

    private static void invalid(
            DeliverableAnalyzerReportLabel label,
            EnumSet<DeliverableAnalyzerReportLabel> labels,
            LabelOperation labelOperation,
            String reason) {
        throw new InvalidLabelOperationException(label, labels, labelOperation, reason);
    }
}