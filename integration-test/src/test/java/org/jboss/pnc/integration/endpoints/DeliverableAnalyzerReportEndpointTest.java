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
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.DeliverableAnalyzerReportClient;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

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
        operationId = operationClient.getAllDeliverableAnalyzerOperation().iterator().next().getId();
    }

    @Test
    public void testGetAnalyzedArtifacts() throws ClientException {
        // given
        DeliverableAnalyzerReportClient client = new DeliverableAnalyzerReportClient(
                RestClientConfiguration.asAnonymous());

        // when
        RemoteCollection<AnalyzedArtifact> analyzedArtifacts = client.getAnalyzedArtifacts(operationId);

        // then
        System.out.println("######## analyzedArtifacts: ");
        analyzedArtifacts.forEach(System.out::println);
        System.out.println("######## operationId: " + operationId);
    }
}
