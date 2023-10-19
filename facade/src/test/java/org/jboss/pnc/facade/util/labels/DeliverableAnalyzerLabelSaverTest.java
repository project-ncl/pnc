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
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JBoss, Home of Professional Open Source. Copyright 2014-2022 Red Hat, Inc., and individual contributors as indicated
 * by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

@RunWith(MockitoJUnitRunner.class)
public class DeliverableAnalyzerLabelSaverTest {

    @Mock
    private UserService userService;

    @Mock
    private DeliverableAnalyzerLabelEntryRepository repository;

    @InjectMocks
    private DeliverableAnalyzerLabelSaverImpl labelSaver;

    private static final User demoUser = User.Builder.newBuilder()
            .username("demo-user")
            .firstName("Demo First Name")
            .lastName("Demo Last Name")
            .email("demo-user@pnc.com")
            .build();

    @Test
    public void testAddLabel() {
        // given
        var oldReport = DeliverableAnalyzerReport.builder()
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        var updatedReport = oldReport.toBuilder().labels(EnumSet.of(DeliverableAnalyzerReportLabel.DELETED)).build();

        // when
        labelSaver.labeledObject = oldReport;
        when(userService.currentUser()).thenReturn(demoUser);
        labelSaver.addLabel(DeliverableAnalyzerReportLabel.DELETED);

        // then
        assertThat(labelSaver.labeledObject).isEqualTo(updatedReport);
        verify(repository).save(
                argThat(
                        (DeliverableAnalyzerLabelEntry entry) -> partiallyEquals(
                                entry,
                                updatedReport,
                                1,
                                LabelOperation.ADDED,
                                DeliverableAnalyzerReportLabel.DELETED)));
    }

    @Test
    public void testRemoveLabel() {
        // given
        var oldReport = DeliverableAnalyzerReport.builder()
                .labels(EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH, DeliverableAnalyzerReportLabel.DELETED))
                .build();
        var updatedReport = oldReport.toBuilder().labels(EnumSet.of(DeliverableAnalyzerReportLabel.SCRATCH)).build();

        // when
        when(userService.currentUser()).thenReturn(demoUser);
        when(repository.getLatestChangeOrderOfReport(any())).thenReturn(42);
        labelSaver.init(oldReport, null);
        labelSaver.removeLabel(DeliverableAnalyzerReportLabel.DELETED);

        // then
        assertThat(labelSaver.labeledObject).isEqualTo(updatedReport);
        verify(repository).save(
                argThat(
                        (DeliverableAnalyzerLabelEntry entry) -> partiallyEquals(
                                entry,
                                updatedReport,
                                43,
                                LabelOperation.REMOVED,
                                DeliverableAnalyzerReportLabel.DELETED)));
    }

    private boolean partiallyEquals(
            DeliverableAnalyzerLabelEntry actualLabelEntry,
            DeliverableAnalyzerReport expectedReport,
            Integer expectedChangeOrder,
            LabelOperation expectedChange,
            DeliverableAnalyzerReportLabel expectedLabel) {
        return actualLabelEntry.getReport().equals(expectedReport)
                && actualLabelEntry.getChangeOrder().equals(expectedChangeOrder)
                && actualLabelEntry.getUser().equals(demoUser) && actualLabelEntry.getChange().equals(expectedChange)
                && actualLabelEntry.getLabel().equals(expectedLabel);
    }
}