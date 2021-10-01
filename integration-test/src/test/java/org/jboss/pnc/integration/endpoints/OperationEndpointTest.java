/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.api.enums.OperationStatus;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class OperationEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(OperationEndpointTest.class);

    private OperationClient client = new OperationClient(RestClientConfiguration.asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldInsertNewOperation() throws Exception {
        // given
        Map<String, String> operationParameters = new HashMap<String, String>();
        operationParameters.put("url-0", "https://github.com/project-ncl/pnc/archive/refs/tags/2.1.1.tar.gz");
        operationParameters.put("url-1", "https://github.com/project-ncl/pnc-common/archive/refs/tags/2.1.0.zip");
        DeliverableAnalyzerOperation operation = DeliverableAnalyzerOperation.delAnalyzerbuilder()
                .id(Sequence.nextBase32Id())
                .startTime(Instant.now())
                .status(OperationStatus.IN_PROGRESS)
                .parameters(operationParameters)
                .build();

        // when
        DeliverableAnalyzerOperation operationReturned = client.createNewOrUpdate(operation);

        // then
        assertThat(operationReturned.getId()).isNotNull().isNotEmpty();
        assertThat(operationReturned.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(operationReturned.getStartTime()).isEqualTo(operation.getStartTime());
        assertThat(operationReturned.getId()).isEqualTo(operation.getId());
    }

    @Test
    public void testGetSpecific() throws ClientException {

        String id = client.getAllDeliverableAnalyzerOperation().iterator().next().getId();

        assertThat(id).isNotNull().isNotEmpty();

        DeliverableAnalyzerOperation operation = client.getSpecific(id);
        assertThat(operation.getId()).isEqualTo(id);
        assertThat(operation.getStatus()).isNotNull();
        assertThat(operation.getStartTime()).isNotNull();
        assertThat(operation.getUser()).isNotNull();
    }

    @Test
    public void shouldUpdateExistingOperation() throws Exception {
        // given
        Map<String, String> operationParameters = new HashMap<String, String>();
        operationParameters.put("url-0", "https://github.com/project-ncl/pnc/archive/refs/tags/2.1.1.tar.gz");
        operationParameters.put("url-1", "https://github.com/project-ncl/pnc-common/archive/refs/tags/2.1.0.zip");
        DeliverableAnalyzerOperation startingOperation = DeliverableAnalyzerOperation.delAnalyzerbuilder()
                .id(Sequence.nextBase32Id())
                .startTime(Instant.now())
                .status(OperationStatus.IN_PROGRESS)
                .parameters(operationParameters)
                .build();

        // when
        DeliverableAnalyzerOperation startedOperationReturned = client.createNewOrUpdate(startingOperation);

        // then
        assertThat(startedOperationReturned.getId()).isNotNull().isNotEmpty();
        assertThat(startedOperationReturned.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(startedOperationReturned.getStartTime()).isEqualTo(startingOperation.getStartTime());
        assertThat(startedOperationReturned.getUser()).isNotNull();
        assertThat(startedOperationReturned.getId()).isEqualTo(startingOperation.getId());

        // Create a new object for the same operation with same id but different values
        operationParameters.put("url-3", "https://github.com/project-ncl/pnc-common/archive/refs/tags/2.0.0.zip");
        DeliverableAnalyzerOperation finishedOperation = DeliverableAnalyzerOperation.delAnalyzerbuilder()
                .id(startingOperation.getId())
                .startTime(Instant.now())
                .status(OperationStatus.SUCCESSFUL)
                .parameters(operationParameters)
                .endTime(Instant.now())
                .build();

        // when
        DeliverableAnalyzerOperation finishedOperationReturned = client.createNewOrUpdate(finishedOperation);

        // then
        assertThat(finishedOperationReturned.getId()).isNotNull().isNotEmpty();
        assertThat(finishedOperationReturned.getUser()).isNotNull();
        assertThat(finishedOperationReturned.getId()).isEqualTo(startedOperationReturned.getId());
        assertThat(finishedOperationReturned.getStartTime()).isEqualTo(startedOperationReturned.getStartTime());
        assertThat(finishedOperationReturned.getStatus()).isEqualTo(OperationStatus.SUCCESSFUL);
        assertThat(finishedOperationReturned.getEndTime()).isEqualTo(finishedOperation.getEndTime());
        assertThat(finishedOperationReturned.getParameters()).hasSize(2);
    }
}
