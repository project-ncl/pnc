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
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeliverableAnalyzerReportLabelModifierTest {

    @Mock
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    @InjectMocks
    private DeliverableAnalyzerReportLabelModifier modifier;

    private static final Base32LongID reportId = new Base32LongID(42L);

    @Test
    public void shouldFailWhenAddingLabelWhichIsAlreadyThere() {
        InvalidLabelOperationException labelOperationException = assertThrows(
                InvalidLabelOperationException.class,
                () -> modifier.addLabelToActiveLabels(
                        reportId,
                        DeliverableAnalyzerReportLabel.SCRATCH,
                        EnumSet.of(DeliverableAnalyzerReportLabel.DELETED, DeliverableAnalyzerReportLabel.SCRATCH)));
        assertThat(labelOperationException.getMessage()).isEqualTo(
                "Unable to add the label SCRATCH to labels: [DELETED, SCRATCH]: label already present in the set of active labels");
    }

    @Test
    public void shouldFailWhenRemovingLabelWhichIsNotThere() {
        InvalidLabelOperationException labelOperationException = assertThrows(
                InvalidLabelOperationException.class,
                () -> modifier.removeLabelFromActiveLabels(
                        reportId,
                        DeliverableAnalyzerReportLabel.SCRATCH,
                        EnumSet.noneOf(DeliverableAnalyzerReportLabel.class)));
        assertThat(labelOperationException.getMessage()).isEqualTo(
                "Unable to remove the label SCRATCH from labels: []: no such label present in the set of active labels");
    }

    @Test
    public void testAddDeletedWhenEmpty() {
        // { } +++DELETED+++> { DELETED }

        generalModifyLabelTestWhenSuccess(
                modifier::addLabelToActiveLabels,
                EnumSet.noneOf(DeliverableAnalyzerReportLabel.class),
                EnumSet.of(DeliverableAnalyzerReportLabel.DELETED),
                DeliverableAnalyzerReportLabel.DELETED);
    }

    @Test
    public void testAddDeletedWhenReleasedOnly() {
        // { RELEASED } +++DELETED+++> { DELETED }

        generalModifyLabelTestWhenSuccess(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED),
                EnumSet.of(DeliverableAnalyzerReportLabel.DELETED),
                DeliverableAnalyzerReportLabel.DELETED);
    }

    @Test
    public void testAddDeletedWhenScratchOnly() {
        // { SCRATCH } +++DELETED+++> { SCRATCH, DELETED }

        generalModifyLabelTestWhenSuccess(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH),
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH, DeliverableAnalyzerReportLabel.DELETED),
                DeliverableAnalyzerReportLabel.DELETED);
    }

    @Test
    public void testAddReleasedWhenEmpty() {
        // { } +++RELEASED+++> { RELEASED }

        generalModifyLabelTestWhenSuccess(
                modifier::addLabelToActiveLabels,
                EnumSet.noneOf(DeliverableAnalyzerReportLabel.class),
                EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED),
                DeliverableAnalyzerReportLabel.RELEASED);
    }

    @Test
    public void testAddReleasedWhenDeletedOnly() {
        // { DELETED } +++RELEASED+++> error

        generalModifyLabelTestWhenFail(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.DELETED),
                DeliverableAnalyzerReportLabel.RELEASED,
                "Unable to add the label RELEASED to labels: [DELETED]: cannot mark as RELEASED the report which is already marked DELETED");
    }

    @Test
    public void testAddReleasedWhenScratchOnly() {
        // { SCRATCH } +++RELEASED+++> error

        generalModifyLabelTestWhenFail(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH),
                DeliverableAnalyzerReportLabel.RELEASED,
                "Unable to add the label RELEASED to labels: [SCRATCH]: cannot mark as RELEASED the report which is already marked SCRATCH");
    }

    @Test
    public void testAddReleasedWhenScratchAndDeleted() {
        // { SCRATCH, DELETED } +++RELEASED+++> error

        generalModifyLabelTestWhenFail(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH, DeliverableAnalyzerReportLabel.DELETED),
                DeliverableAnalyzerReportLabel.RELEASED,
                "Unable to add the label RELEASED to labels: [DELETED, SCRATCH]: cannot mark as RELEASED the report which is already marked DELETED");
    }

    @Test
    public void testAddScratchShouldFail1() {
        // { } +++SCRATCH+++> error

        generalModifyLabelTestWhenFail(
                modifier::addLabelToActiveLabels,
                EnumSet.noneOf(DeliverableAnalyzerReportLabel.class),
                DeliverableAnalyzerReportLabel.SCRATCH,
                "Unable to add the label SCRATCH to labels: []: label can be marked as SCRATCH only when the analysis is executed");
    }

    @Test
    public void testAddScratchShouldFail2() {
        // { RELEASED } +++SCRATCH+++> error

        generalModifyLabelTestWhenFail(
                modifier::addLabelToActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED),
                DeliverableAnalyzerReportLabel.SCRATCH,
                "Unable to add the label SCRATCH to labels: [RELEASED]: label can be marked as SCRATCH only when the analysis is executed");
    }

    @Test
    public void testRemoveDeletedWhenDeletedOnly() {
        // { DELETED } ---DELETED---> { }

        generalModifyLabelTestWhenSuccess(
                modifier::removeLabelFromActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.DELETED),
                EnumSet.noneOf(DeliverableAnalyzerReportLabel.class),
                DeliverableAnalyzerReportLabel.DELETED);
    }

    @Test
    public void testRemoveDeletedWhenDeletedAndScratch() {
        // { DELETED, SCRATCH } ---DELETED---> { SCRATCH }

        generalModifyLabelTestWhenSuccess(
                modifier::removeLabelFromActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.DELETED, DeliverableAnalyzerReportLabel.SCRATCH),
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH),
                DeliverableAnalyzerReportLabel.DELETED);
    }

    @Test
    public void testRemoveReleasedWhenReleasedOnly() {
        // { RELEASED } ---RELEASED---> { }

        generalModifyLabelTestWhenSuccess(
                modifier::removeLabelFromActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED),
                EnumSet.noneOf(DeliverableAnalyzerReportLabel.class),
                DeliverableAnalyzerReportLabel.RELEASED);
    }

    @Test
    public void testRemoveScratchWhenScratchOnly() {
        // { SCRATCH } ---SCRATCH---> error

        generalModifyLabelTestWhenFail(
                modifier::removeLabelFromActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH),
                DeliverableAnalyzerReportLabel.SCRATCH,
                "Unable to remove the label SCRATCH from labels: [SCRATCH]: label marked SCRATCH cannot be removed");
    }

    @Test
    public void testRemoveScratchWhenScratchAndReleased() {
        // { SCRATCH, RELEASED } ---SCRATCH---> error

        generalModifyLabelTestWhenFail(
                modifier::removeLabelFromActiveLabels,
                EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH, DeliverableAnalyzerReportLabel.RELEASED),
                DeliverableAnalyzerReportLabel.SCRATCH,
                "Unable to remove the label SCRATCH from labels: [SCRATCH, RELEASED]: label marked SCRATCH cannot be removed");
    }

    private void generalModifyLabelTestWhenSuccess(
            DeliverableAnalyzerReportLabelUpdateFunction functionProvider,
            EnumSet<DeliverableAnalyzerReportLabel> nonUpdatedLabels,
            EnumSet<DeliverableAnalyzerReportLabel> updatedLabels,
            DeliverableAnalyzerReportLabel labelToBeApplied) {
        // given
        var reportWithoutUpdatedLabels = DeliverableAnalyzerReport.builder()
                .id(reportId)
                .labels(nonUpdatedLabels)
                .build();
        var reportWithUpdatedLabels = reportWithoutUpdatedLabels.toBuilder().labels(updatedLabels).build();

        // when
        when(deliverableAnalyzerReportRepository.queryById(reportId)).thenReturn(reportWithoutUpdatedLabels);
        functionProvider.accept(reportId, labelToBeApplied, nonUpdatedLabels);

        // then
        verify(deliverableAnalyzerReportRepository).save(reportWithUpdatedLabels);
    }

    private void generalModifyLabelTestWhenFail(
            DeliverableAnalyzerReportLabelUpdateFunction functionProvider,
            EnumSet<DeliverableAnalyzerReportLabel> nonUpdatedLabels,
            DeliverableAnalyzerReportLabel labelToBeApplied,
            String expectedExceptionMessage) {
        InvalidLabelOperationException labelOperationException = assertThrows(
                InvalidLabelOperationException.class,
                () -> functionProvider.accept(reportId, labelToBeApplied, nonUpdatedLabels));
        assertThat(labelOperationException.getMessage()).isEqualTo(expectedExceptionMessage);
    }
}