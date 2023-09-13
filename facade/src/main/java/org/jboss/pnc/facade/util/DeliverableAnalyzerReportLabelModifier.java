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

import javax.ejb.Stateless;
import java.util.EnumSet;

/**
 * {@link LabelModifier} for {@link DeliverableAnalyzerReportLabel} entity.
 */
@Stateless
public class DeliverableAnalyzerReportLabelModifier implements LabelModifier<DeliverableAnalyzerReportLabel> {

    @Override
    public void addLabel(DeliverableAnalyzerReportLabel label, EnumSet<DeliverableAnalyzerReportLabel> labels)
            throws InvalidLabelOperationException {
        checkLabelIsNotPresent(label, labels);

        switch (label) {
            case DELETED:
                labels.add(label);
                break;
            case RELEASED:
                if (labels.contains(DeliverableAnalyzerReportLabel.DELETED)) {
                    throw new InvalidLabelOperationException(
                            label,
                            labels,
                            LabelOperation.ADDED,
                            "cannot mark as RELEASED the report which is already marked DELETED");
                } else if (labels.contains(DeliverableAnalyzerReportLabel.SCRATCH)) {
                    throw new InvalidLabelOperationException(
                            label,
                            labels,
                            LabelOperation.ADDED,
                            "cannot mark as RELEASED the report which is already marked SCRATCH");
                }
                labels.add(label);
                break;
            case SCRATCH:
                throw new InvalidLabelOperationException(
                        label,
                        labels,
                        LabelOperation.ADDED,
                        "label can be marked as 'SCRATCH' only when the analysis is executed");
            default:
                throw new UnsupportedOperationException("Adding of label " + label + " is not supported");
        }
    }

    @Override
    public void removeLabel(DeliverableAnalyzerReportLabel label, EnumSet<DeliverableAnalyzerReportLabel> labels)
            throws InvalidLabelOperationException {
        checkLabelIsPresent(label, labels);

        switch (label) {
            case DELETED:
            case RELEASED:
            case SCRATCH:
                labels.remove(label);
                break;
            default:
                throw new UnsupportedOperationException("Deleting of label " + label + " is not supported");
        }
    }
}
