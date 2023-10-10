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
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.DeliverableAnalyzerReportClient;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.demo.data.DatabaseDataInitializer;
import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.demo.data.DatabaseDataInitializer.*;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class DeliverableAnalyzerReportEndpointTest {

    private static String operationId;

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
    }

    @Test
    public void testGetAnalyzedArtifacts() throws ClientException {
        // given
        DeliverableAnalyzerReportClient client = new DeliverableAnalyzerReportClient(
                RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<AnalyzedArtifact> analyzedArtifacts = client.getAnalyzedArtifacts(operationId);

        // then
        assertThat(analyzedArtifacts.size()).isEqualTo(2);

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

    @Ignore
    @Test
    public void testGetLabelHistory() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(labelHistory.size()).isEqualTo(2);

        Iterator<DeliverableAnalyzerLabelEntry> labelHistoryIterator = labelHistory.iterator();
        var firstLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(firstLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.SCRATCH);
        assertThat(firstLabelHistoryEntry.getReason()).isEqualTo("This analysis was run as scratch!");

        var secondLabelHistoryEntry = labelHistoryIterator.next();
        assertThat(secondLabelHistoryEntry.getLabel()).isEqualTo(DeliverableAnalyzerReportLabel.SCRATCH);
        assertThat(secondLabelHistoryEntry.getReason())
                .isEqualTo("This was actually quite successful, removing SCRATCH");
    }

    @Test
    @InSequence(10)
    public void testRemoveLabelEntry() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asSystem());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.SCRATCH)
                .reason("It's perfect, let's make it RELEASED!")
                .build();

        // when
        client.removeLabel(operationId, request);
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(labelHistory.size()).isEqualTo(3);
    }

    @Test
    @InSequence(20)
    public void testAddLabelEntry() throws ClientException {
        // given
        var client = new DeliverableAnalyzerReportClient(RestClientConfiguration.asSystem());
        DeliverableAnalyzerReportLabelRequest request = DeliverableAnalyzerReportLabelRequest.builder()
                .label(DeliverableAnalyzerReportLabel.RELEASED)
                .reason("This was the true diamond, releasing..")
                .build();

        // when
        client.addLabel(operationId, request);
        RemoteCollection<DeliverableAnalyzerLabelEntry> labelHistory = client.getLabelHistory(operationId);

        // then
        assertThat(labelHistory.size()).isEqualTo(4);
    }
}
