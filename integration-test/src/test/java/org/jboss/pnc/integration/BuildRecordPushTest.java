/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.BuildRecordPushRestClient;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.mock.CausewayClientMock;
import org.jboss.pnc.mock.executor.BuildExecutorMock;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordPushTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(BuildRecordPushTest.class);

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        JavaArchive processManager = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.PROCESS_MANAGERS_JAR);
        processManager.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        processManager.addClass(CausewayClientMock.class);
        processManager.addClass(BuildExecutorMock.class);

        return enterpriseArchive;
    }

    @Test
    public void shouldPushBuildRecord() {

        BuildRecordRestClient buildRecordRestClient = new BuildRecordRestClient();
        BuildRecordRest buildRecordRest = buildRecordRestClient.firstNotNull().getValue();
        Integer buildRecordId = buildRecordRest.getId();

        BuildRecordPushRestClient pushRestClient = new BuildRecordPushRestClient();

        BuildRecordPushRequestRest pushRequest = new BuildRecordPushRequestRest("tagPrefix", buildRecordId);
        RestResponse<Map> restResponse = pushRestClient.push(pushRequest);

        Map<String, Boolean> responseValue = restResponse.getValue();

        Assertions.assertThat(responseValue.get(buildRecordId.toString())).isTrue();

    }

}
