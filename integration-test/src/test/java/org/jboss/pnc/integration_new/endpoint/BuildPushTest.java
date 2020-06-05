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
package org.jboss.pnc.integration_new.endpoint;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.causewayclient.DefaultCausewayClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.mock.client.CausewayClientMock;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jboss.pnc.integration_new.setup.Deployments.addBuildExecutorMock;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildPushTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpointTest.class);
    private static final String ID = "1234";
    private static String buildId;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.testEar();

        JavaArchive processManager = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.CAUSEWAY_CLIENT_JAR);
        processManager.deleteClass(DefaultCausewayClient.class);
        processManager.addClass(CausewayClientMock.class);

        processManager.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        addBuildExecutorMock(enterpriseArchive);

        return enterpriseArchive;
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        Iterator<Build> it = bc.getAll(null, null).iterator();
        buildId = it.next().getId();
    }

    @Test
    public void shouldPushBuild() throws ClientException {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        Build build = client.getSpecific(buildId);

        // first push accepted
        BuildPushParameters parameters = BuildPushParameters.builder().reimport(false).tagPrefix("test-tag").build();
        BuildPushResult result = client.push(build.getId(), parameters);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BuildPushStatus.ACCEPTED);

        // second push rejected because already in process
        assertThatThrownBy(() -> client.push(build.getId(), parameters)).hasCauseInstanceOf(ClientErrorException.class);

        // successful complete of first push
        client.completePush(buildId, returnSuccessfulResult(buildId));

        // get result from db
        BuildPushResult successPushResult = client.getPushResult(buildId);

        assertThat(successPushResult.getStatus()).isEqualTo(BuildPushStatus.SUCCESS);
        assertThat(successPushResult.getLogContext()).isEqualTo(ID);

        // next push should accept again
        BuildPushResult result2 = client.push(build.getId(), parameters);

        assertThat(result2).isNotNull();
        assertThat(result2.getStatus()).isEqualTo(BuildPushStatus.ACCEPTED);
    }

    private BuildPushResult returnSuccessfulResult(String id) {
        return BuildPushResult.builder().status(BuildPushStatus.SUCCESS).buildId(id).id(ID).build();
    }
}