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
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.requests.ScratchDeliverablesAnalysisRequest;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.integration.utils.BPMWireMock;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class OperationEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(OperationEndpointTest.class);

    private OperationClient client = new OperationClient(RestClientConfiguration.asUser());

    private static BPMWireMock bpm;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void beforeClass() {
        var bpmPort = 8288;
        bpm = new BPMWireMock(bpmPort);
        logger.info("Mocked BPM started at port: " + bpmPort);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        bpm.close();
        logger.info("Mocked BPM stopped");
    }

    @Test
    public void testGetSpecificDeliverableAnalyzer() throws ClientException {

        String id = client.getAllDeliverableAnalyzerOperation().iterator().next().getId();

        assertThat(id).isNotNull().isNotEmpty();

        DeliverableAnalyzerOperation operation = client.getSpecificDeliverableAnalyzer(id);
        assertThat(operation.getId()).isEqualTo(id);
        assertThat(operation.getProgressStatus()).isNotNull();
        assertThat(operation.getSubmitTime()).isNotNull();
        assertThat(operation.getUser()).isNotNull();
    }

    @Test
    public void shouldUpdateExistingOperation() throws Exception {
        OperationClient client = new OperationClient(RestClientConfiguration.asSystem());
        // given
        String id = LongBase32IdConverter.toString(1000001l);

        DeliverableAnalyzerOperation originalOperation = client.getSpecificDeliverableAnalyzer(id);
        assertThat(originalOperation).isNotNull();
        assertThat(originalOperation.getProgressStatus()).isNotEqualTo(ProgressStatus.FINISHED);
        assertThat(originalOperation.getResult()).isNotEqualTo(OperationResult.SUCCESSFUL);
        assertThat(originalOperation.getParameters()).hasSize(1);

        // Create a new object for the same operation with same id but different values
        Map<String, String> operationParameters = new HashMap<String, String>(originalOperation.getParameters());
        operationParameters.put("url-3", "https://github.com/project-ncl/pnc-common/archive/refs/tags/2.0.0.zip");
        DeliverableAnalyzerOperation finishedOperation = DeliverableAnalyzerOperation.delAnalyzerBuilder()
                .id(id)
                .startTime(Instant.now())
                .progressStatus(ProgressStatus.FINISHED)
                .result(OperationResult.SUCCESSFUL)
                .parameters(operationParameters)
                .endTime(Instant.now())
                .build();

        // when
        DeliverableAnalyzerOperation finishedOperationReturned = client
                .updateDeliverableAnalyzer(id, finishedOperation);

        // then
        assertThat(finishedOperationReturned.getId()).isNotNull().isNotEmpty();
        assertThat(finishedOperationReturned.getUser()).isNotNull();
        assertThat(finishedOperationReturned.getId()).isEqualTo(id);
        assertThat(finishedOperationReturned.getStartTime()).isEqualTo(originalOperation.getStartTime());
        assertThat(finishedOperationReturned.getProgressStatus()).isEqualTo(ProgressStatus.FINISHED);
        assertThat(finishedOperationReturned.getResult()).isEqualTo(OperationResult.SUCCESSFUL);
        assertThat(finishedOperationReturned.getEndTime())
                .isEqualTo(finishedOperation.getEndTime().truncatedTo(ChronoUnit.MILLIS)); // Date conversion looses
        // microseconds
        assertThat(finishedOperationReturned.getParameters()).hasSize(1);
    }

    @Test
    public void shouldFinishOperation() throws Exception {
        // given
        String id = LongBase32IdConverter.toString(1000002l);

        DeliverableAnalyzerOperation originalOperation = client.getSpecificDeliverableAnalyzer(id);
        assertThat(originalOperation).isNotNull();
        assertThat(originalOperation.getProgressStatus()).isNotEqualTo(ProgressStatus.FINISHED);
        assertThat(originalOperation.getResult()).isNull();
        assertThat(originalOperation.getEndTime()).isNull();

        // when
        Instant beforeFinish = Instant.now();
        client.finish(id, OperationResult.FAILED);

        DeliverableAnalyzerOperation finishedOperationReturned = client.getSpecificDeliverableAnalyzer(id);

        // then
        assertThat(finishedOperationReturned.getId()).isNotNull().isNotEmpty();
        assertThat(finishedOperationReturned.getUser()).isNotNull();
        assertThat(finishedOperationReturned.getId()).isEqualTo(id);
        assertThat(finishedOperationReturned.getStartTime()).isEqualTo(originalOperation.getStartTime());
        assertThat(finishedOperationReturned.getProgressStatus()).isEqualTo(ProgressStatus.FINISHED);
        assertThat(finishedOperationReturned.getResult()).isEqualTo(OperationResult.FAILED);
        assertThat(finishedOperationReturned.getEndTime()).isAfter(beforeFinish);
    }

    @Test
    public void shouldScratchFlagBeTrue() throws ClientException {
        // when
        client.startScratchDeliverableAnalysis(
                ScratchDeliverablesAnalysisRequest.builder()
                        .deliverablesUrls(
                                List.of(
                                        "https://indy.psi.idk.com/api/content/maven/hosted/pnc-builds/com/jboss/super-important.jar"))
                        .build());

        // then
        bpm.getWireMockServer()
                .verify(
                        postRequestedFor(urlMatching(".*")).withRequestBody(
                                matching(".*super-important.jar.*")
                                        .and(matching(".*\"runAsScratchAnalysis\":true.*"))));
    }

    @Test
    public void shouldHaveNonNullId() throws ClientException {
        // when
        DeliverableAnalyzerOperation startedDelAnOperation = client.startScratchDeliverableAnalysis(
                ScratchDeliverablesAnalysisRequest.builder()
                        .deliverablesUrls(
                                List.of(
                                        "https://indy.psi.idk.com/api/content/maven/hosted/pnc-builds/com/jboss/super-important.jar"))
                        .build());

        // then
        assertThat(startedDelAnOperation.getId()).isNotNull();
    }
}
