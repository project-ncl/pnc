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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Comparator;
import java.util.Iterator;

import javax.ws.rs.ForbiddenException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildPushesClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.OperationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.requests.GroupBuildPushRequest;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildPushTest {

    // does not exist
    private static final String PUSH_OPERATION_ID = "AAAAAAAAAAAAA";

    private static String buildId;
    private static String groupBuildId;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        BuildClient bc = new BuildClient(RestClientConfiguration.asUser());
        RemoteCollection<Build> builds = bc.getAll(null, null);

        // Sort by ID to retain IDs in the test
        // After, NCL-8156 the default ordering was fixed and changed to submitTime
        Iterator<Build> it = builds.getAll()
                .stream()
                .sorted(Comparator.comparingLong(build -> new Base32LongID(build.getId()).getLongId()))
                .iterator();

        buildId = it.next().getId();

        GroupBuildClient gbc = new GroupBuildClient(RestClientConfiguration.asUser());
        RemoteCollection<GroupBuild> groupBuilds = gbc.getAll();
        groupBuildId = groupBuilds.iterator().next().getId();
    }

    @Test
    public void shouldRejectBuildPushEndpoints() throws ClientException {
        BuildClient client = new BuildClient(RestClientConfiguration.asUser());
        BuildPushParameters parameters = BuildPushParameters.builder().reimport(false).tagPrefix("test-tag").build();
        BuildPushCompleted completion = BuildPushCompleted.builder()
                .operationId(PUSH_OPERATION_ID)
                .brewBuildId(12)
                .brewBuildUrl("https://example.com/build/12")
                .callback(Request.builder().build())
                .build();

        assertThatThrownBy(() -> client.push(buildId, parameters)).hasCauseInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> client.cancelPush(buildId)).hasCauseInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> client.completePush(buildId, completion)).hasCauseInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> client.getPushResult(buildId)).hasCauseInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> client.getPushOperations(buildId)).hasCauseInstanceOf(ForbiddenException.class);
    }

    @Test
    public void shouldRejectGroupBuildPush() {
        GroupBuildClient client = new GroupBuildClient(RestClientConfiguration.asUser());
        GroupBuildPushRequest request = GroupBuildPushRequest.builder().tagPrefix("test-tag").build();

        assertThatThrownBy(() -> client.brewPush(groupBuildId, request)).hasCauseInstanceOf(ForbiddenException.class);
    }

    @Test
    public void shouldRejectBuildPushOperationEndpoints() {
        OperationClient client = new OperationClient(RestClientConfiguration.asUser());

        assertThatThrownBy(() -> client.getSpecificBuildPush(PUSH_OPERATION_ID))
                .hasCauseInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> client.getAllBuildPushOperation()).hasCauseInstanceOf(ForbiddenException.class);
    }

    @Test
    public void shouldRejectBuildPushReport() {
        BuildPushesClient client = new BuildPushesClient(RestClientConfiguration.asUser());

        assertThatThrownBy(() -> client.getPushReport(PUSH_OPERATION_ID)).hasCauseInstanceOf(ForbiddenException.class);
    }
}
