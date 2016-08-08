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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.AbstractRestClient;
import org.jboss.pnc.integration.client.BuildRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.env.IntegrationTestEnv;
import org.jboss.pnc.integration.utils.AuthUtils;
import org.jboss.pnc.mock.coordinator.BuildCoordinatorMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Before;
import org.junit.Ignore;
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
import static org.mockito.Mockito.when;


@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class UserRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private static BuildRestClient buildRestClient;
    private static UserRestClient userRestClient;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildCoordinatorMock buildCoordinatorMock;


    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addAsWebInfResource(new StringAsset(
                Descriptors.create(BeansDescriptor.class).
                        getOrCreateAlternatives().
                        clazz(BuildCoordinatorMock.class.
                                getName()).up().exportAsString()), "beans.xml");
        war.addClass(BuildCoordinatorMock.class);
        war.addClass(UserRestTest.class);
        addRestClientClasses(war);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    protected static void addRestClientClasses(WebArchive war) {
        war.addClass(BuildRestClient.class);
        war.addClass(AbstractRestClient.class);
        war.addClass(AuthUtils.class);
        war.addClass(IntegrationTestEnv.class);
        war.addClass(RestResponse.class);

        war.addClass(UserRestClient.class);
    }

    @Before
    public void before() {
        if(buildRestClient == null) {
            buildRestClient = new BuildRestClient();
        }
        if(userRestClient == null) {
            userRestClient = new UserRestClient();
        }
        buildCoordinatorMock.clearActiveTasks();
    }

    @Test
    @InSequence(-1)
    public void sanityTests() throws Exception {

        assertThat(buildRecordRepository.queryAll().stream()
                .filter(record -> record.getUser().getId().equals(1) && record.getUser().getUsername().equals("demo-user"))
                .count())
                .isEqualTo(2)
                .overridingErrorMessage("2 BuildRecords in DB expected with user.id == 1 and user.username == \"demo-user\"");

        assertThat(buildCoordinatorMock).isNotNull();
    }

    @Test
    public void shouldGetAllRunningAndCompletedBuildRecordsForUserId() throws Exception {
        // given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));
        buildCoordinatorMock.addActiveTask(mockBuildTask(102, 1, "demo-user"));

        // when
        RestResponse<List<BuildRecordRest>> all = userRestClient.allUserBuilds(1);

        // then
        assertThat(all.getValue()).hasSize(4);
    }

    @Test
    public void shouldOnlyGetBuildsForGivenUserId() throws Exception {
        // given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));
        buildCoordinatorMock.addActiveTask(mockBuildTask(103, 101, "little-bobby-tables"));

        // when
        RestResponse<List<BuildRecordRest>> all = userRestClient.allUserBuilds(1);

        // then
        assertThat(all.getValue()).hasSize(3);
    }

    @Ignore
    @Test
    public void shouldSortResults() throws Exception {
        //given
        String sort = "=desc=id";

        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));

        //when
        List<Integer> sorted = userRestClient.allUserBuilds(1, true, 0, 50, null, sort).getValue().stream()
                .map(BuildRecordRest::getId)
                .collect(Collectors.toList());

        //then
        assertThat(sorted).containsExactly(101, 2, 1);
    }

    @Ignore
    @Test
    public void shouldSupportPaging() throws Exception {
        //given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));

        //when
        List<BuildRecordRest> firstPage = userRestClient.allUserBuilds(1, true, 0, 1, null, null).getValue();
        List<BuildRecordRest> secondPage = userRestClient.allUserBuilds(1, true, 1, 1, null, null).getValue();
        List<BuildRecordRest> thirdPage = userRestClient.allUserBuilds(1, true, 2, 1, null, null).getValue();


        //then
        assertThat(firstPage).hasSize(1);
        assertThat(secondPage).hasSize(1);
        assertThat(thirdPage).hasSize(1);
    }

    @Ignore
    @Test
    public void shouldBeAbleToReachAllBuildsWhenPaging() throws Exception {
        //given
        String sort = "=desc=id";

        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));

        //when
        List<BuildRecordRest> firstPage = buildRestClient.all(true, 0, 1, null, sort).getValue();
        List<BuildRecordRest> secondPage = buildRestClient.all(true, 1, 1, null, sort).getValue();
        List<BuildRecordRest> thirdPage = buildRestClient.all(true, 2, 1, null, sort).getValue();

        //then
        assertThat(firstPage.get(0).getId()).isEqualTo(99);
        assertThat(secondPage.get(0).getId()).isEqualTo(2);
        assertThat(thirdPage.get(0).getId()).isEqualTo(1);
    }

    @Ignore
    @Test
    public void shouldReturnCorrectPageCount() throws Exception {
        // Given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 1, "demo-user"));

        // When
        int totalPages = userRestClient.allUserBuilds(1, true, 0, 1, null, null).getRestCallResponse().getBody().jsonPath().getInt("totalPages");

        //then
        assertThat(totalPages).isEqualTo(3);
    }


    protected BuildTask mockBuildTask(int id, int userId, String username) {
        BuildTask mockedTask = mock(BuildTask.class);
        doReturn(id).when(mockedTask).getId();
        doReturn(mock(User.class)).when(mockedTask).getUser();
        doReturn(mock(BuildConfiguration.class)).when(mockedTask).getBuildConfiguration();
        doReturn(mock(BuildConfigurationAudited.class)).when(mockedTask).getBuildConfigurationAudited();
        when(mockedTask.getBuildConfigurationAudited().getIdRev()).thenReturn(mock(IdRev.class));
        when(mockedTask.getBuildConfigurationAudited().getIdRev().getId()).thenReturn(99);
        when(mockedTask.getUser().getId()).thenReturn(userId);
        when(mockedTask.getUser().getUsername()).thenReturn(username);
        return mockedTask;
    }
}
