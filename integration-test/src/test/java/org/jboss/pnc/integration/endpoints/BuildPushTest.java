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
import org.jboss.pnc.bpm.causeway.FinalLogRestProducer;
import org.jboss.pnc.causewayclient.DefaultCausewayClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.mock.client.BifrostMock;
import org.jboss.pnc.integration.mock.client.CausewayClientMock;
import org.jboss.pnc.integration.mock.client.FinalLogRestMock;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.Base32LongID;
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
import javax.ws.rs.ForbiddenException;
import java.util.Comparator;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildPushTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpointTest.class);
    private static String buildId;
    private static String build2Id;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.testEar();

        JavaArchive causewayClientJar = enterpriseArchive.getAsType(JavaArchive.class, Deployments.CAUSEWAY_CLIENT_JAR);
        causewayClientJar.deleteClass(DefaultCausewayClient.class);
        causewayClientJar.addClass(CausewayClientMock.class);

        JavaArchive processManager = enterpriseArchive.getAsType(JavaArchive.class, Deployments.BPM_JAR);
        processManager.deleteClass(FinalLogRestProducer.class);
        processManager.addClass(BifrostMock.class);
        processManager.addClass(FinalLogRestMock.class);

        processManager.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");

        return enterpriseArchive;
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<Build> builds = bc.getAll(null, null);

        // Sort by ID to retain IDs in the test
        // After, NCL-8156 the default ordering was fixed and changed to submitTime
        Iterator<Build> it = builds.getAll()
                .stream()
                .sorted(Comparator.comparingLong(build -> new Base32LongID(build.getId()).getLongId()))
                .iterator();

        buildId = it.next().getId();
        build2Id = it.next().getId();
    }

    @Test
    public void shouldPushBuild() throws ClientException {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        Build build = client.getSpecific(buildId);

        // first push accepted
        BuildPushParameters parameters = BuildPushParameters.builder().reimport(false).tagPrefix("test-tag").build();
        BuildPushResult result = client.push(build.getId(), parameters);
        String buildPushResultId = result.getId();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BuildPushStatus.ACCEPTED);

        // second push rejected because already in process
        assertThatThrownBy(() -> client.push(build.getId(), parameters)).hasCauseInstanceOf(ClientErrorException.class);

        // should reject completion with invalid result id
        assertThatThrownBy(() -> client.completePush(buildId, returnSuccessfulResult(buildId, "1111")))
                .hasCauseInstanceOf(ClientErrorException.class);

        // successful complete of first push
        client.completePush(buildId, returnSuccessfulResult(buildId, buildPushResultId));

        // get result from db
        BuildPushResult successPushResult = client.getPushResult(buildId);

        assertThat(successPushResult.getStatus()).isEqualTo(BuildPushStatus.SUCCESS);
        assertThat(successPushResult.getLogContext()).isEqualTo(buildPushResultId);

        // next push should accept again
        BuildPushResult result2 = client.push(build.getId(), parameters);

        assertThat(result2).isNotNull();
        assertThat(result2.getStatus()).isEqualTo(BuildPushStatus.ACCEPTED);
    }

    @Test
    public void shouldRefuseToPushNoRebuildRequiredStatusBuild() throws ClientException {
        BuildClient client = new BuildClient(RestClientConfiguration.asSystem());
        Build build = client.getSpecific(build2Id);
        Build noRebuildStatus = build.toBuilder().status(BuildStatus.NO_REBUILD_REQUIRED).build();

        client.update(build2Id, noRebuildStatus);

        BuildPushParameters parameters = BuildPushParameters.builder().reimport(false).tagPrefix("test-tag").build();

        assertThatThrownBy(() -> client.push(build2Id, parameters)).hasCauseInstanceOf(ForbiddenException.class);
    }

    private BuildPushResult returnSuccessfulResult(String id, String buildPushResultId) {
        return BuildPushResult.builder().status(BuildPushStatus.SUCCESS).buildId(id).id(buildPushResultId).build();
    }
}