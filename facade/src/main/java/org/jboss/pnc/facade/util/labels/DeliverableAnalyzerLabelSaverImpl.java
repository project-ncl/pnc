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
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

/**
 * Concrete implementation of {@link LabelSaver} for {@link DeliverableAnalyzerReport}.
 */
@RequestScoped
public class DeliverableAnalyzerLabelSaverImpl extends
        LabelSaver<Base32LongID, Base32LongID, DeliverableAnalyzerReportLabel, DeliverableAnalyzerLabelEntry, DeliverableAnalyzerReport>
        implements DeliverableAnalyzerLabelSaver {

    private final UserService userService;

    @Inject
    public DeliverableAnalyzerLabelSaverImpl(
            DeliverableAnalyzerLabelEntryRepository repository,
            UserService userService) {
        super(repository);

        this.userService = userService;
    }

    @Override
    public void addLabel(DeliverableAnalyzerReportLabel label) {
        labeledObject.getLabels().add(label);
        labelEntryRepository.save(createAddLabelHistoryEntry(label));
    }

    @Override
    public void removeLabel(DeliverableAnalyzerReportLabel label) {
        labeledObject.getLabels().remove(label);
        labelEntryRepository.save(createRemoveLabelHistoryEntry(label));
    }

    private DeliverableAnalyzerLabelEntry createAddLabelHistoryEntry(DeliverableAnalyzerReportLabel labelToAdd) {
        return createLabelHistoryWithChange(labelToAdd, LabelOperation.ADDED);
    }

    private DeliverableAnalyzerLabelEntry createRemoveLabelHistoryEntry(DeliverableAnalyzerReportLabel labelToRemove) {
        return createLabelHistoryWithChange(labelToRemove, LabelOperation.REMOVED);
    }

    private DeliverableAnalyzerLabelEntry createLabelHistoryWithChange(
            DeliverableAnalyzerReportLabel label,
            LabelOperation change) {
        return DeliverableAnalyzerLabelEntry.builder()
                .report(labeledObject)
                .changeOrder(++nextChangeOrder)
                .user(userService.currentUser())
                .entryTime(Date.from(Instant.now()))
                .reason(reason)
                .label(label)
                .change(change)
                .build();
    }
}
