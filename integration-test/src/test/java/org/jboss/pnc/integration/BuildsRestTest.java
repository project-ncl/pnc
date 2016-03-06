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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.integration.client.AbstractRestClient;
import org.jboss.pnc.integration.client.BuildRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.env.IntegrationTestEnv;
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.mock.coordinator.BuildCoordinatorMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SuppressWarnings("ALL")
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildsRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static BuildRestClient buildRestClient;

    @Inject
    private BuildRecordProvider buildRecordProvider;

    @Inject
    BuildCoordinatorMock buildCoordinatorMock;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        war.addClass(BuildsRestTest.class);
        war.addClass(BuildCoordinatorMock.class);
        addRestClientClasses(war);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    protected static void addRestClientClasses(WebArchive war) {
        war.addClass(BuildRestClient.class);
        war.addClass(AbstractRestClient.class);
        war.addClass(AuthResource.class);
        war.addClass(IntegrationTestEnv.class);
        war.addClass(RestResponse.class);
        war.addAsResource("auth.properties");
    }

    @Before
    public void before() {
        if(buildRestClient == null) {
            buildRestClient = new BuildRestClient();
        }
        buildCoordinatorMock.clearActiveTasks();
    }

    @Test
    @InSequence(-1)
    public void sanityTests() throws Exception {
        assertThat(buildRecordProvider.getAll(0, 99, "", "").getContent())
                .hasSize(2)
                .overridingErrorMessage("2 BuildRecords are expected in DB before running this test");

        assertThat(buildCoordinatorMock).isNotNull();
    }

    @Test
    public void shouldGetAllArchivedBuildRecords() throws Exception {
        //given
        buildCoordinatorMock.addActiveTask(mockBuildTask());
        buildCoordinatorMock.addActiveTask(mockBuildTask());

        //when
        RestResponse<List<BuildRecordRest>> all = buildRestClient.all();

        //then
        assertThat(all.getValue()).hasSize(4);
    }

    @Test
    public void shouldSortResults() throws Exception {
        //given
        String sort = "=desc=id";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        //when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, null, sort).getValue().stream()
                .map(value -> value.getId())
                .collect(Collectors.toList());

        //then
        assertThat(sorted).containsExactly(99, 2, 1);
    }

    @Test
    public void shouldFilterResults() throws Exception {
        //given
        String rsql = "id==1";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        //when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, rsql, null).getValue().stream()
                .map(value -> value.getId())
                .collect(Collectors.toList());

        //then
        assertThat(sorted).containsExactly(1);
    }

    @Test
    public void shouldSupportPaging() throws Exception {
        //given
        String sort = "=desc=id";

        buildCoordinatorMock.addActiveTask(mockBuildTask());

        //when
        List<BuildRecordRest> firstPage = buildRestClient.all(true, 0, 1, null, sort).getValue();
        List<BuildRecordRest> secondPage = buildRestClient.all(true, 1, 1, null, sort).getValue();

        //then
        assertThat(firstPage).hasSize(1);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    public void shouldFilterByUserId() throws Exception {
        // given
        String rsql = "user.id==1";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        // when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, rsql, null).getValue().stream().map(value -> value.getId())
                .collect(Collectors.toList());

        // then
        assertThat(sorted).containsExactly(2);
    }

    @Test
    public void shouldFilterByUsername() throws Exception {
        // given
        String rsql = "user.username==pnc-admin";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        // when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, rsql, null).getValue().stream().map(value -> value.getId())
                .collect(Collectors.toList());

        // then
        assertThat(sorted).containsExactly(2);
    }

    @Test
    public void shouldFilterByBuildConfigurationId() throws Exception {
        // given
        String rsql = "buildConfigurationAudited.idRev.id==1";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        // when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, rsql, null).getValue().stream().map(value -> value.getId())
                .collect(Collectors.toList());

        // then
        assertThat(sorted).containsExactly(1);
    }

    @Test
    public void shouldFilterByBuildConfigurationName() throws Exception {
        // given
        String rsql = "buildConfigurationAudited.name==jboss-modules-1.5.0";

        BuildTask mockedTask = mockBuildTask();
        buildCoordinatorMock.addActiveTask(mockedTask);

        // when
        List<Integer> sorted = buildRestClient.all(true, 0, 50, rsql, null).getValue().stream().map(value -> value.getId())
                .collect(Collectors.toList());

        // then
        assertThat(sorted).containsExactly(1);
    }

    protected BuildTask mockBuildTask() {
        BuildTask mockedTask = mock(BuildTask.class);
        doReturn(99).when(mockedTask).getId();
        doReturn(mock(User.class)).when(mockedTask).getUser();
        doReturn(mock(BuildConfiguration.class)).when(mockedTask).getBuildConfiguration();
        doReturn(mock(BuildConfigurationAudited.class)).when(mockedTask).getBuildConfigurationAudited();
        return mockedTask;
    }
}
