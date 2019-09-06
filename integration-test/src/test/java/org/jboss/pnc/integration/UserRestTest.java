/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.BuildRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.AuthUtils;
import org.jboss.pnc.mock.coordinator.BuildCoordinatorMock;
import org.jboss.pnc.mock.model.BuildConfigurationMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
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
    private static BuildConfigurationAudited buildConfigurationAudited;
    private static BuildConfigurationRest filterTestBuildConfiguration;


    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    SequenceHandlerRepository sequenceHandlerRepository;

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
        war.addClass(BuildConfigurationMock.class);
        war.addClass(UserRestTest.class);

        war.addClass(AuthUtils.class);
        Deployments.addRestClients(enterpriseArchive);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
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
    @Transactional
    public void prepare() throws Exception {

        Set<BuildRecord> buildRecords = buildRecordRepository.queryAll().stream()
                .filter(record -> record.getUser().getId().equals(100) && record.getUser().getUsername().equals("demo-user"))
                .collect(Collectors.toSet());
        assertThat(buildRecords.size())
                .isEqualTo(2)
                .overridingErrorMessage("2 BuildRecords in DB expected with user.id == 100 and user.username == \"demo-user\"");

        assertThat(buildCoordinatorMock).isNotNull();
        BuildRecord buildRecord = buildRecords.iterator().next();
        logger.debug("Found buildRecord {}", buildRecord);
        IdRev buildConfigurationAuditedIdRev = buildRecord.getBuildConfigurationAuditedIdRev();
        buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(buildConfigurationAuditedIdRev);
        logger.debug("Found buildConfigurationAudited {}", buildConfigurationAudited);

        filterTestBuildConfiguration = insertNewBuildConfiguration(
                buildConfigurationAudited.getProject(),
                buildConfigurationAudited.getBuildEnvironment(),
                buildConfigurationAudited.getRepositoryConfiguration());
    }


    /*
     * Tests for /users/{id}/builds endpoint
     */

    @Test
    public void shouldGetAllRunningAndCompletedBuildRecordsForUserId() throws Exception {
        // given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));
        buildCoordinatorMock.addActiveTask(mockBuildTask(102, 100, "demo-user"));

        // when
        RestResponse<List<BuildRecordRest>> all = userRestClient.allUserBuilds(100);

        // then
        assertThat(all.getValue()).hasSize(4);
    }

    @Test
    public void shouldOnlyGetBuildsForGivenUserId() throws Exception {
        // given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));
        buildCoordinatorMock.addActiveTask(mockBuildTask(103, 101, "little-bobby-tables"));

        // when
        RestResponse<List<BuildRecordRest>> all = userRestClient.allUserBuilds(100);

        // then
        assertThat(all.getValue()).hasSize(3);
    }

    @Test
    public void shouldSortResultsWhenGettingUserBuilds() throws Exception {
        //given
        String sort = "=desc=id";

        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));

        //when
        List<Integer> sorted = userRestClient.allUserBuilds(100, true, 0, 50, null, sort).getValue().stream()
                .map(BuildRecordRest::getId)
                .collect(Collectors.toList());

        //then
        assertThat(sorted).containsExactly(101, 2, 1);
    }

    @Test
    public void shouldSupportPagingWhenGettingUserBuilds() throws Exception {
        //given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));

        //when
        List<BuildRecordRest> firstPage = userRestClient.allUserBuilds(100, true, 0, 1, null, null).getValue();
        List<BuildRecordRest> secondPage = userRestClient.allUserBuilds(100, true, 1, 1, null, null).getValue();
        List<BuildRecordRest> thirdPage = userRestClient.allUserBuilds(100, true, 2, 1, null, null).getValue();


        //then
        assertThat(firstPage).hasSize(1);
        assertThat(secondPage).hasSize(1);
        assertThat(thirdPage).hasSize(1);
    }

    @Test
    public void shouldBeAbleToReachAllBuildsWhenPaging() throws Exception {
        //given
        String sort = "=desc=id";

        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));

        //when
        List<BuildRecordRest> firstPage = buildRestClient.all(true, 0, 1, null, sort).getValue();
        List<BuildRecordRest> secondPage = buildRestClient.all(true, 1, 1, null, sort).getValue();
        List<BuildRecordRest> thirdPage = buildRestClient.all(true, 2, 1, null, sort).getValue();

        //then
        assertThat(firstPage.get(0).getId()).isEqualTo(101);
        assertThat(secondPage.get(0).getId()).isEqualTo(2);
        assertThat(thirdPage.get(0).getId()).isEqualTo(1);
    }

    @Test
    public void shouldReturnCorrectPageCountWhenGettingUserBuilds() throws Exception {
        // Given
        buildCoordinatorMock.addActiveTask(mockBuildTask(101, 100, "demo-user"));

        // When
        int totalPages = userRestClient.allUserBuilds(100, true, 0, 1, null, null).getRestCallResponse().getBody().jsonPath().getInt("totalPages");

        //then
        assertThat(totalPages).isEqualTo(3);
    }

    @Test
    public void shouldSupportFilteringByBcIdWhenGettingUserBuilds() throws Exception {
        // given
        Integer buildConfigurationId = filterTestBuildConfiguration.getId();
        String rsql = "buildConfigurationId==" + buildConfigurationId;

        int mockBuildId = 846839;
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.findLatestById(buildConfigurationId);
        buildCoordinatorMock.addActiveTask(mockBuildTask(mockBuildId, 100, "demo-user", buildConfigurationAudited));
        // when
        List<Integer> sorted = userRestClient.allUserBuilds(100, true, 0, 50, rsql, null).getValue()
                .stream()
                .map(BuildRecordRest::getId)
                .collect(Collectors.toList());

        // then
        assertThat(sorted).containsExactly(mockBuildId);
    }

    private BuildConfigurationRest insertNewBuildConfiguration(Project project,
            BuildEnvironment buildEnvironment,
            RepositoryConfiguration repositoryConfiguration) {
        BuildConfiguration buildConfiguration = BuildConfigurationMock.createNew(null, project, buildEnvironment, repositoryConfiguration);
        BuildConfigurationRestClient buildConfigurationRestClient = new BuildConfigurationRestClient();
        RestResponse<BuildConfigurationRest> aNew = buildConfigurationRestClient.createNew(new BuildConfigurationRest(buildConfiguration));
        return aNew.getValue();
    }

    @Test
    public void shouldFilterByNonExistantBuildConfigurationIdWhenGettingUserBuilds() throws Exception {
        // given
        String rsql = "buildConfigurationId==9000";

        // when
        List<Integer> sorted = userRestClient.allUserBuilds(100, true, 0, 50, rsql, null).getValue()
                .stream()
                .map(BuildRecordRest::getId)
                .collect(Collectors.toList());

        // then
        assertThat(sorted).isEmpty();
    }

    @Test
    public void shouldUpdateUsersRequiredFields() {
        // given
        UserRest user = new UserRest();
        user.setEmail("shouldUpdateUsersRequiredFields@pnc.com");
        user.setUsername("shouldUpdateUsersRequiredFields");

        RestResponse<UserRest> response = userRestClient.createNew(user);
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(201);
        user = response.getValue();

        // when
        String newEmail = "new@pnc.com";
        String newUsername = "newPncUser";
        UserRest userWithNewValues = new UserRest();
        userWithNewValues.setUsername(newUsername);
        userWithNewValues.setEmail(newEmail);
        RestResponse<UserRest> updateResponse = userRestClient.update(user.getId(), userWithNewValues);

        // then
        assertThat(updateResponse.getRestCallResponse().getStatusCode()).isEqualTo(200);
        UserRest updatedUser = userRestClient.get(user.getId()).getValue();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    public void shouldUpdateUsersNotRequiredFields() {
        // given
        String email = "shouldUpdateUsersNotRequiredFields@pnc.com";
        String username = "shouldUpdateUsersNotRequiredFields";
        UserRest user = new UserRest();
        user.setEmail(email);
        user.setUsername(username);

        RestResponse<UserRest> response = userRestClient.createNew(user);
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(201);
        user = response.getValue();

        // when
        String firstName = "firstName";
        String lastName = "lastName";
        UserRest userWithNewValues = new UserRest();
        userWithNewValues.setEmail(email);
        userWithNewValues.setUsername(username);
        userWithNewValues.setFirstName(firstName);
        userWithNewValues.setLastName(lastName);
        RestResponse<UserRest> updateResponse = userRestClient.update(user.getId(), userWithNewValues);

        // then
        assertThat(updateResponse.getRestCallResponse().getStatusCode()).isEqualTo(200);
        UserRest updatedUser = userRestClient.get(user.getId()).getValue();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(email);
        assertThat(updatedUser.getUsername()).isEqualTo(username);
        assertThat(updatedUser.getFirstName()).isEqualTo(firstName);
        assertThat(updatedUser.getLastName()).isEqualTo(lastName);
    }

    protected BuildTask mockBuildTask(int id, int userId, String username) {
        return mockBuildTask(id, userId, username, buildConfigurationAudited);
    }

    protected BuildTask mockBuildTask(int id, int userId, String username, BuildConfigurationAudited buildConfigurationAudited) {
        BuildTask mockedTask = mock(BuildTask.class);
        doReturn(id).when(mockedTask).getId();
        doReturn(mock(User.class)).when(mockedTask).getUser();
        doReturn(buildConfigurationAudited).when(mockedTask).getBuildConfigurationAudited();
        when(mockedTask.getUser().getId()).thenReturn(userId);
        when(mockedTask.getUser().getUsername()).thenReturn(username);
        when(mockedTask.getBuildOptions()).thenReturn(new BuildOptions());
        return mockedTask;
    }
}
