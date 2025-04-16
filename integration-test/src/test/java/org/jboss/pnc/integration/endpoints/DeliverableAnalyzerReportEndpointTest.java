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
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.DeliverableAnalyzerReportClient;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.DeliverableAnalyzerReport;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class DeliverableAnalyzerReportEndpointTest {

    private static String operationId;
    private static String operationId2;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        OperationClient operationClient = new OperationClient(RestClientConfiguration.asAnonymous());
        Iterator<DeliverableAnalyzerOperation> it = operationClient.getAllDeliverableAnalyzerOperation().iterator();
        it.next();
        operationId = it.next().getId();
        operationId2 = it.next().getId();
    }

    @Test
    public void shouldGetDeliverableAnalyzerReports() throws RemoteResourceException {
        DeliverableAnalyzerReportClient client = new DeliverableAnalyzerReportClient(
                RestClientConfiguration.asAnonymous());
        RemoteCollection<DeliverableAnalyzerReport> all = client.getAll();

        assertThat(all).hasSize(11);
    }

    @Test
    public void shouldGetSpecificDeliverableAnalyzerReport() throws ClientException {
        DeliverableAnalyzerReportClient client = new DeliverableAnalyzerReportClient(
                RestClientConfiguration.asAnonymous());

        DeliverableAnalyzerReport report = client.getSpecific(operationId);

        assertThat(report.getId()).isEqualTo(operationId);
        assertThat(report.getLabels()).containsOnly(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(report.getUser()).isNotNull();
        assertThat(report.getStartTime()).isNotNull();
    }

    @Test
    public void testGetAnalyzedArtifacts() throws ClientException {
        // given
        DeliverableAnalyzerReportClient client = new DeliverableAnalyzerReportClient(
                RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<AnalyzedArtifact> analyzedArtifacts = client.getAnalyzedArtifacts(operationId);

        // then
        assertThat(analyzedArtifacts.size()).isEqualTo(7); // analyzedArtifact1-7 (from DatabaseDataInitializer)

        Iterator<AnalyzedArtifact> it = analyzedArtifacts.iterator();

        AnalyzedArtifact analyzedArtifact0 = it.next();
        assertThat(analyzedArtifact0.isBuiltFromSource()).isTrue();
        assertThat(analyzedArtifact0.getBrewId()).isNull();
        assertThat(analyzedArtifact0.getArtifact().getIdentifier()).isEqualTo("demo:built-artifact1:jar:1.0");

        AnalyzedArtifact analyzedArtifact1 = it.next();
        assertThat(analyzedArtifact1.isBuiltFromSource()).isFalse();
        assertThat(analyzedArtifact1.getBrewId()).isNull();
        assertThat(analyzedArtifact1.getArtifact().getIdentifier()).isEqualTo("demo:imported-artifact2:jar:1.0");
    }

    @Test
    public void testGetLabelHistory() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(labelHistory.size()).isEqualTo(1);

        Iterator<DeliverableAnalyzerLabelEntry> labelHistoryIterator = labelHistory.iterator();
        var firstLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(firstLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(firstLabelHistoryEntry.getReason()).isEqualTo("This was a game-changer! Release it! <3");
    }

    @Test
    @InSequence(10)
    public void testAddLabelEntry() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asUser());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.DELETED)
                .reason("Oh jeez, this was clearly a mistake, marking as DELETED..")
                .build();

        // when
        assertThat(client.getLabelHistory(operationId).size()).isOne();
        client.addLabel(operationId, request);
        DeliverableAnalyzerReport report = client.getSpecific(operationId);
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(report.getLabels()).containsExactly(DeliverableAnalyzerReportLabel.DELETED);

        assertThat(labelHistory.size()).isEqualTo(3);

        Iterator<DeliverableAnalyzerLabelEntry> labelHistoryIterator = labelHistory.iterator();
        var firstLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(firstLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.ADDED);
        assertThat(firstLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(firstLabelHistoryEntry.getReason()).isEqualTo("This was a game-changer! Release it! <3");

        var secondLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(secondLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.REMOVED);
        assertThat(secondLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(secondLabelHistoryEntry.getReason())
                .isEqualTo("Oh jeez, this was clearly a mistake, marking as DELETED..");

        var thirdLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(thirdLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.ADDED);
        assertThat(thirdLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.DELETED);
        assertThat(thirdLabelHistoryEntry.getReason())
                .isEqualTo("Oh jeez, this was clearly a mistake, marking as DELETED..");
    }

    @Test
    @InSequence(10)
    public void testAddFirstLabelEntry() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asUser());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.DELETED)
                .reason("Oh jeez, this was clearly a mistake, marking as DELETED..")
                .build();

        // when
        assertThat(client.getLabelHistory(operationId2).size()).isZero();
        client.addLabel(operationId2, request);
        DeliverableAnalyzerReport report = client.getSpecific(operationId2);
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId2);

        // then
        assertThat(report.getLabels()).containsExactly(DeliverableAnalyzerReportLabel.DELETED);

        assertThat(labelHistory.size()).isEqualTo(1);

        Iterator<DeliverableAnalyzerLabelEntry> labelHistoryIterator = labelHistory.iterator();
        var labelHistoryEntry = labelHistoryIterator.next();
        assertThat(labelHistoryEntry.getChange()).isEqualTo(LabelOperation.ADDED);
        assertThat(labelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.DELETED);
        assertThat(labelHistoryEntry.getReason())
                .isEqualTo("Oh jeez, this was clearly a mistake, marking as DELETED..");
    }

    @Test
    @InSequence(20)
    public void testRemoveLabelEntry() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asUser());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.DELETED)
                .reason("Nvm, let it live")
                .build();

        // when
        client.removeLabel(operationId, request);
        DeliverableAnalyzerReport report = client.getSpecific(operationId);
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(report.getLabels()).isEmpty();

        assertThat(labelHistory.size()).isEqualTo(4);

        Iterator<DeliverableAnalyzerLabelEntry> labelHistoryIterator = labelHistory.iterator();
        var firstLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(firstLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.ADDED);
        assertThat(firstLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(firstLabelHistoryEntry.getReason()).isEqualTo("This was a game-changer! Release it! <3");

        var secondLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(secondLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.REMOVED);
        assertThat(secondLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.RELEASED);
        assertThat(secondLabelHistoryEntry.getReason())
                .isEqualTo("Oh jeez, this was clearly a mistake, marking as DELETED..");

        var thirdLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(thirdLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.ADDED);
        assertThat(thirdLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.DELETED);
        assertThat(thirdLabelHistoryEntry.getReason())
                .isEqualTo("Oh jeez, this was clearly a mistake, marking as DELETED..");

        var fourthLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(fourthLabelHistoryEntry.getChange()).isEqualTo(LabelOperation.REMOVED);
        assertThat(fourthLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.DELETED);
        assertThat(fourthLabelHistoryEntry.getReason()).isEqualTo("Nvm, let it live");
    }

    @Test
    @InSequence(30)
    public void shouldThrowExceptionWhenRemovingNonexistent() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asUser());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.DELETED)
                .reason("Let's try to remove something what is not here")
                .build();

        // then
        try {
            client.removeLabel(operationId, request);
            fail("Expecting this test method to throw exception when adding duplicate label");
        } catch (RemoteResourceException ex) {
            assertThat(ex.getCause().getMessage()).isEqualTo("HTTP 409 Conflict");
        }
    }
}
