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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration.mock.ConfigurationPersistentBuildingDisabledMock;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.jboss.pnc.demo.data.DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.asUser;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class PersistentBuildingTest {

    private static final Logger logger = LoggerFactory.getLogger(PersistentBuildingTest.class);
    private BuildClient buildClient = new BuildClient(asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        final EnterpriseArchive ear = Deployments.testEarForInContainerTest(PersistentBuildingTest.class);

        JavaArchive coordinatorJar = ear.getAsType(JavaArchive.class, Deployments.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-remote-clients-and-local-scheduler.xml", "beans.xml");
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);

        // NCL-9910 -- disallow persistent building via a CDI @Alternative that overrides Configuration
        JavaArchive facadeJar = ear.getAsType(JavaArchive.class, "/facade.jar");
        facadeJar.addClass(ConfigurationPersistentBuildingDisabledMock.class);
        facadeJar.addAsManifestResource("beans-persistent-building-disabled.xml", "beans.xml");

        return ear;
    }

    @Test
    public void shouldNotTriggerPersistentBuildAndFinishWithException() throws ClientException {
        // with
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(asUser());
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll()
                .getAll()
                .stream()
                .filter(bc -> bc.getName().equals(PNC_PROJECT_BUILD_CFG_ID))
                .iterator()
                .next();

        // when
        assertThatThrownBy(
                () -> buildConfigurationClient.trigger(buildConfiguration.getId(), getBuildParameters(false, true)))
                        .isInstanceOf(RemoteResourceException.class)
                        .hasMessageContaining(
                                "Triggering persistent builds is currently disabled, only temporary builds triggering is allowed.");
    }

    @Test
    public void shouldTriggerTemporaryBuildAndFinishWithoutProblems() throws ClientException {
        // with
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(asUser());
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll()
                .getAll()
                .stream()
                .filter(bc -> bc.getName().equals("termd"))
                .iterator()
                .next();

        // when
        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), getBuildParameters(true, true));
        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);
    }

    private BuildParameters getBuildParameters(boolean temporary, boolean force) {
        BuildParameters buildParameters = new BuildParameters();

        buildParameters.setTemporaryBuild(temporary);
        buildParameters.setBuildDependencies(true);
        if (force)
            buildParameters.setRebuildMode(RebuildMode.FORCE);

        return buildParameters;
    }

    private Boolean buildToFinish(String buildId, EnumSet<BuildStatus> isIn, EnumSet<BuildStatus> isNotIn) {
        Build build = null;
        logger.debug("Waiting for build {} to finish", buildId);
        try {
            build = buildClient.getSpecific(buildId);
            assertThat(build).isNotNull();
            logger.debug("Gotten build with status: {}", build.getStatus());
            if (!build.getStatus().isFinal())
                return false;
        } catch (RemoteResourceNotFoundException e) {
            fail(String.format("Build with id:%s not present", buildId), e);
        } catch (ClientException e) {
            fail("Client has failed in an unexpected way.", e);
        }
        assertThat(build).isNotNull();
        assertThat(build.getStatus()).isNotNull();
        if (isIn != null && !isIn.isEmpty())
            assertThat(build.getStatus()).isIn(isIn);
        if (isNotIn != null && !isNotIn.isEmpty())
            assertThat(build.getStatus()).isNotIn(isNotIn);
        return true;
    }
}
