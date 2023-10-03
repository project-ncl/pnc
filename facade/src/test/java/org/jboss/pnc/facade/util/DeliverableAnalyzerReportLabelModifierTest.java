package org.jboss.pnc.facade.util;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeliverableAnalyzerReportLabelModifierTest {

    @Mock
    private DeliverableAnalyzerReportRepository deliverableAnalyzerReportRepository;

    @InjectMocks
    private DeliverableAnalyzerReportLabelModifier modifier;

    @Test
    public void testAddReleasedWhenEmpty() {
        // { } ---RELEASED---> { RELEASED }

        // given
        var reportId = new Base32LongID(42L);
        EnumSet<DeliverableAnalyzerReportLabel> activeLabels = EnumSet.noneOf(DeliverableAnalyzerReportLabel.class);
        var reportWithoutUpdatedLabels = DeliverableAnalyzerReport.builder()
                .id(reportId)
                .labels(EnumSet.noneOf(DeliverableAnalyzerReportLabel.class))
                .build();
        var reportWithUpdatedLabels = reportWithoutUpdatedLabels.toBuilder()
                .labels(EnumSet.of(DeliverableAnalyzerReportLabel.RELEASED))
                .build();

        // when
        when(deliverableAnalyzerReportRepository.queryById(reportId)).thenReturn(reportWithoutUpdatedLabels);
        modifier.addLabelToActiveLabels(reportId, DeliverableAnalyzerReportLabel.RELEASED, activeLabels);

        // then
        verify(deliverableAnalyzerReportRepository).save(reportWithUpdatedLabels);
    }
}