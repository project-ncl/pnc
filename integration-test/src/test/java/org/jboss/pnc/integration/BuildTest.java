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
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.client.BuildConfigSetRecordRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.BuildConfigurationSetRestClient;
import org.jboss.pnc.integration.client.BuildRecordRestClient;
import org.jboss.pnc.integration.client.BuildRestClient;
import org.jboss.pnc.integration.client.UserRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildTest {

    protected static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static BuildConfigurationRestClient buildConfigurationRestClient;
    private static BuildConfigurationSetRestClient buildConfigurationSetRestClient;
    private static BuildRecordRestClient buildRecordRestClient;
    private static BuildConfigSetRecordRestClient buildConfigSetRecordRestClient;
    private static BuildRestClient buildRestClient;
    private static UserRestClient userRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        restWar.addClass(BuildTest.class);
        restWar.addAsWebInfResource("beans-use-mock-remote-clients.xml", "beans.xml");

        JavaArchive coordinatorJar = enterpriseArchive.getAsType(JavaArchive.class, AbstractTest.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        addBuildExecutorMock(enterpriseArchive);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(buildConfigurationRestClient == null) {
            buildConfigurationRestClient = new BuildConfigurationRestClient();
        }
        if(buildConfigurationSetRestClient == null) {
            buildConfigurationSetRestClient = new BuildConfigurationSetRestClient();
        }
        if(buildRecordRestClient == null) {
            buildRecordRestClient = new BuildRecordRestClient();
        }
        if (buildRestClient == null) {
            buildRestClient = new BuildRestClient();
        }
        if(userRestClient == null) {
            userRestClient = new UserRestClient();
            userRestClient.createUser("admin");
            userRestClient.createUser("user");
        }

        if (buildConfigSetRecordRestClient == null) {
            buildConfigSetRecordRestClient = new BuildConfigSetRecordRestClient();
        }
    }


    // The test is not running the actual build it uses BuildExecutorMock
    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() throws Exception {
        logger.debug("Running shouldTriggerBuildAndFinishWithoutProblems");

        //given
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.firstNotNull().getValue();

        //when
        RestResponse<UserRest> loggedUser = userRestClient.getLoggedUser();//initialize user
        logger.debug("LoggedUser: {}", loggedUser.hasValue() ? loggedUser.getValue() : "-no-logged-user-");

        logger.info("About to trigger build: {} with id: {}.", buildConfiguration.getName(), buildConfiguration.getId());

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setForceRebuild(true);
        RestResponse<BuildConfigurationRest> triggeredConfiguration = buildConfigurationRestClient.trigger(buildConfiguration.getId(), buildOptions);
        logger.debug("Response from triggered build: {}.", triggeredConfiguration.hasValue() ? triggeredConfiguration.getValue() : "-response-not-available-");
        Integer buildRecordId = ResponseUtils.getIdFromLocationHeader(triggeredConfiguration.getRestCallResponse());
        logger.info("New build record id: {}.", buildRecordId);

        //then
        assertThat(triggeredConfiguration.getRestCallResponse().getStatusCode()).isEqualTo(200);

        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildRecordId, false).hasValue(), 15, TimeUnit.SECONDS);
    }

    // The test is not running the actual build it uses BuildExecutorMock
    @Test
    public void shouldTriggerBuildSetAndFinishWithoutProblems() throws Exception {
        //given
        BuildConfigurationSetRest buildConfigurationSet = buildConfigurationSetRestClient.firstNotNull().getValue();

        //when
        userRestClient.getLoggedUser(); //initialize user
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setForceRebuild(false);
        RestResponse<BuildConfigurationSetRest> response = buildConfigurationSetRestClient.trigger(buildConfigurationSet.getId(), buildOptions);
        Integer buildRecordSetId = ResponseUtils.getIdFromLocationHeader(response.getRestCallResponse());

        //then
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(200);
        assertThat(buildRecordSetId).isNotNull();

        ResponseUtils.waitSynchronouslyFor(() -> buildConfigSetRecordRestClient.get(buildRecordSetId, false).hasValue(), 15, TimeUnit.SECONDS);

        assertThat(buildConfigSetRecordRestClient.get(buildRecordSetId, false).getValue().getStatus())
                .isNotEqualTo(BuildStatus.REJECTED);
    }
}
