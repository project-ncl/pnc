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
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.integration.utils.ResponseUtils;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.asUser;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildTest.class);

    private BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(asUser());

    private GroupConfigurationClient groupConfigurationClient = new GroupConfigurationClient(asUser());

    private GroupBuildClient groupBuildClient = new GroupBuildClient(asUser());

    private BuildClient buildClient = new BuildClient(asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        final EnterpriseArchive ear = Deployments.testEarForInContainerTest(BuildTest.class);
        Deployments.addBuildExecutorMock(ear);
        JavaArchive coordinatorJar = ear.getAsType(JavaArchive.class, Deployments.COORDINATOR_JAR);
        coordinatorJar.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        coordinatorJar.addClass(RemoteBuildsCleanerMock.class);
        return ear;
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() throws ClientException {
        // with
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll().iterator().next();

        // when
        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), getPersistentParameters(true));
        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerGroupBuildAndFinishWithoutProblems() throws ClientException {
        // given
        GroupConfiguration groupConfig = groupConfigurationClient.getAll().iterator().next();

        // when
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);

        GroupBuild groupBuild = groupConfigurationClient.trigger(
                groupConfig.getId(),
                groupBuildParameters,
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());
        assertThat(groupBuild).isNotNull().extracting("id").isNotNull().isNotEqualTo("");
        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldRejectGroupBuildWithNoRebuildsRequired() throws ClientException {
        // given
        GroupConfiguration groupConfig = groupConfigurationClient.getAll().iterator().next();

        // and after one build is done
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);
        GroupBuild groupBuild1 = groupConfigurationClient.trigger(
                groupConfig.getId(),
                groupBuildParameters,
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());
        assertThat(groupBuild1).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        ResponseUtils.waitSynchronouslyFor(
                () -> groupBuildToFinish(groupBuild1.getId(), EnumSet.of(BuildStatus.SUCCESS), null),
                15,
                TimeUnit.SECONDS);

        // when next build is triggered
        GroupBuild groupBuild2 = groupConfigurationClient.trigger(
                groupConfig.getId(),
                new GroupBuildParameters(),
                GroupBuildRequest.builder().buildConfigurationRevisions(new ArrayList<>()).build());

        // then
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.SUCCESS, BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> groupBuildToFinish(groupBuild2.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerGroupBuildWithBCInRevisionAndFinishWithoutProblems() throws ClientException {
        // given
        GroupConfiguration groupConfiguration = groupConfigurationClient.getAll().iterator().next();
        assertThat(groupConfiguration.getBuildConfigs()).isNotEmpty();

        List<BuildConfigurationRevisionRef> buildConfigurationRevisions = new ArrayList<>();
        BuildConfigurationRevision buildConfigurationRevision = BuildConfigurationRevision.builder()
                .id(groupConfiguration.getBuildConfigs().keySet().iterator().next())
                .rev(1)
                .name(groupConfiguration.getName())
                .build();
        buildConfigurationRevisions.add(buildConfigurationRevision);

        GroupBuildRequest groupConfigWithAuditedBCsRest = GroupBuildRequest.builder()
                .buildConfigurationRevisions(buildConfigurationRevisions)
                .build();
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setRebuildMode(RebuildMode.FORCE);

        // when
        GroupBuild groupBuild = groupConfigurationClient
                .trigger(groupConfiguration.getId(), groupBuildParameters, groupConfigWithAuditedBCsRest);
        // then
        assertThat(groupBuild).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED);
        ResponseUtils.waitSynchronouslyFor(
                () -> groupBuildToFinish(groupBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerBuildWithADependencyAndFinishWithoutProblems() throws ClientException {
        // given - A BC with a dependency on pnc-1.0.0.DR1
        BuildConfiguration buildConfigurationParent = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==dependency-analysis-1.3"))
                .iterator()
                .next();

        // Update dependency
        BuildConfiguration buildConfigurationChild = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-1.0.0.DR1"))
                .iterator()
                .next();
        BuildConfiguration updatedBuildConfigurationChild = buildConfigurationChild.toBuilder()
                .description(buildConfigurationChild.getDescription() + ".")
                .build();

        buildConfigurationClient.update(buildConfigurationChild.getId(), updatedBuildConfigurationChild);

        // The update of the description should not have changed the lastModificationDate
        assertThat(buildConfigurationChild.getModificationTime())
                .isEqualTo(updatedBuildConfigurationChild.getModificationTime());

        // when
        Build build = buildConfigurationClient
                .trigger(buildConfigurationParent.getId(), getBuildParameters(false, true));

        BuildsFilterParameters parameters = new BuildsFilterParameters();
        parameters.setRunning(true);
        RemoteCollection<Build> childBuildCol = buildConfigurationClient
                .getBuilds(buildConfigurationChild.getId(), parameters);

        Build childBuild = childBuildCol.getAll().iterator().next();
        // then
        assertThat(childBuildCol.size()).isEqualTo(1);
        assertThat(buildConfigurationParent.getDependencies().size()).isEqualTo(1);

        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);

        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(childBuild.getId(), isIn, null), 15, TimeUnit.SECONDS);
    }

    @Test
    public void shouldBuildTemporaryBuildAndNotAssignItToMilestone() throws ClientException {
        // BC pnc-1.0.0.DR1 is assigned to a product version containing an active product milestone see
        // DatabaseDataInitializer#initiliazeProjectProductData
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-1.0.0.DR1"))
                .iterator()
                .next();

        // when

        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), getTemporaryParameters(true));

        // then

        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId()), 15, TimeUnit.SECONDS);

        Build updatedBuild = buildClient.getSpecific(build.getId());
        assertThat(updatedBuild.getProductMilestone()).isNull();
    }

    @Test
    public void shouldTriggerPersistentWithoutForceAfterTemporaryOnTheSameRev() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + " clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        updatedConfiguration = buildConfigurationClient.getSpecific(updatedConfiguration.getId());
        assertThat(oldLastModDate).isNotEqualTo(updatedConfiguration.getModificationTime());
        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId(), isIn, isNotIn), 15, TimeUnit.SECONDS);

        Build afterTempPersistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(afterTempPersistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);
    }

    // NCL-5192
    // Replicates NCL-5192 through explicit dependency instead of implicit
    @Test
    public void dontRebuildTemporaryBuildWhenThereIsNewerPersistentOnSameRev() throws ClientException {
        BuildConfiguration parent = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==pnc-build-agent-0.4"))
                .iterator()
                .next();
        BuildConfiguration dependency = buildConfigurationClient.getAll(Optional.empty(), Optional.of("name==termd"))
                .iterator()
                .next();

        Instant oldLastModDateParent = parent.getModificationTime();
        BuildConfiguration updatedParent = parent.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + "  clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedParent.getId(), updatedParent);
        updatedParent = buildConfigurationClient.getSpecific(updatedParent.getId());
        assertThat(oldLastModDateParent).isNotEqualTo(updatedParent.getModificationTime());

        Instant oldLastModDateDependency = parent.getModificationTime();
        BuildConfiguration updatedDependency = dependency.toBuilder()
                .description("Random Description so it rebuilds")
                .buildScript("mvn" + "   clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedDependency.getId(), updatedDependency);
        assertThat(oldLastModDateDependency).isNotEqualTo(updatedDependency.getModificationTime());

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build temporary builds (parent and dependency) on new revision
        Build temporaryBuild = buildConfigurationClient.trigger(parent.getId(), getTemporaryParameters());
        ResponseUtils
                .waitSynchronouslyFor(() -> buildToFinish(temporaryBuild.getId(), isIn, isNotIn), 15, TimeUnit.SECONDS);

        // Build persistent build of dependency on the same revision
        Build dependencyPersistentBuild = buildConfigurationClient
                .trigger(dependency.getId(), getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(dependencyPersistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Build temporary build of parent and check it gets REJECTED even if it's dependency has newer record
        // (in this case temp build should ignore persistent one)
        Build finalRecord = buildConfigurationClient.trigger(parent.getId(), getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(finalRecord.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldRejectAfterBuildingTwoTempBuildsOnSameRevision() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .buildScript("mvn" + "   clean deploy -DskipTests=true")
                .build();
        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);

        Build temporaryBuild = buildConfigurationClient.trigger(updatedConfiguration.getId(), getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(temporaryBuild.getId(), EnumSet.of(BuildStatus.SUCCESS), null),
                15,
                TimeUnit.SECONDS);

        Build secondTempBuild = buildConfigurationClient
                .trigger(updatedConfiguration.getId(), getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(
                        secondTempBuild.getId(),
                        EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED),
                        EnumSet.of(BuildStatus.SUCCESS, BuildStatus.REJECTED)),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldNotTriggerANewPersistentBuildWithoutForceIfOnlyDescriptionChanged() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build persistent builds (parent and dependency) on new revision
        Build persistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), getPersistentParameters(true));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(persistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that persistent build will be first on this revision")
                .build();

        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        assertThat(oldLastModDate).isEqualTo(updatedConfiguration.getModificationTime());

        Build build2 = buildConfigurationClient.trigger(updatedConfiguration.getId(), getPersistentParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(build2.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldNotTriggerANewTemporaryBuildWithoutForceIfOnlyDescriptionChanged() throws ClientException {
        BuildConfiguration buildConfiguration = buildConfigurationClient
                .getAll(Optional.empty(), Optional.of("name==maven-plugin-test"))
                .iterator()
                .next();

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.REJECTED, BuildStatus.NO_REBUILD_REQUIRED);

        // Build temporary builds (parent and dependency) on new revision
        Build persistentBuild = buildConfigurationClient
                .trigger(buildConfiguration.getId(), getTemporaryParameters(true));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(persistentBuild.getId(), isIn, isNotIn),
                15,
                TimeUnit.SECONDS);

        // Updating the description only won't create a new revision, as description is not audited anymore
        Instant oldLastModDate = buildConfiguration.getModificationTime();
        BuildConfiguration updatedConfiguration = buildConfiguration.toBuilder()
                .description(
                        "Random Description to be able to trigger build again so that temporary build will be first on this revision")
                .build();

        buildConfigurationClient.update(updatedConfiguration.getId(), updatedConfiguration);
        assertThat(oldLastModDate).isEqualTo(updatedConfiguration.getModificationTime());

        Build build2 = buildConfigurationClient.trigger(updatedConfiguration.getId(), getTemporaryParameters());
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(build2.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), null),
                15,
                TimeUnit.SECONDS);
    }

    @Test
    public void shouldHaveNoRebuildCauseFilled() throws Exception {
        // with
        BuildConfiguration buildConfiguration = buildConfigurationClient.getAll().iterator().next();

        // when #1
        Build build = buildConfigurationClient.trigger(buildConfiguration.getId(), getPersistentParameters(true));
        assertThat(build).isNotNull().extracting("id").isNotNull().isNotEqualTo("");

        EnumSet<BuildStatus> isIn = EnumSet.of(BuildStatus.SUCCESS);
        ResponseUtils.waitSynchronouslyFor(() -> buildToFinish(build.getId(), isIn, null), 15, TimeUnit.SECONDS);

        // when #2
        EnumSet<BuildStatus> isNotIn = EnumSet.of(BuildStatus.SUCCESS, BuildStatus.FAILED);
        Build rebuild = buildConfigurationClient.trigger(buildConfiguration.getId(), getBuildParameters(false, false));
        ResponseUtils.waitSynchronouslyFor(
                () -> buildToFinish(rebuild.getId(), EnumSet.of(BuildStatus.NO_REBUILD_REQUIRED), isNotIn),
                15,
                TimeUnit.SECONDS);

        // then
        Build refresh = buildClient.getSpecific(rebuild.getId());
        assertThat(refresh.getNoRebuildCause()).isNotNull().extracting("id").isEqualTo(build.getId());
    }

    private BuildParameters getTemporaryParameters() {
        return getBuildParameters(true, false);
    }

    private BuildParameters getPersistentParameters() {
        return getBuildParameters(false, false);
    }

    private BuildParameters getTemporaryParameters(boolean force) {
        return getBuildParameters(true, force);
    }

    private BuildParameters getPersistentParameters(boolean force) {
        return getBuildParameters(false, force);
    }

    private BuildParameters getBuildParameters(boolean temporary, boolean force) {
        BuildParameters buildParameters = new BuildParameters();

        buildParameters.setTemporaryBuild(temporary);
        buildParameters.setBuildDependencies(true);
        if (force)
            buildParameters.setRebuildMode(RebuildMode.FORCE);

        return buildParameters;
    }

    private Boolean buildToFinish(String id) {
        return buildToFinish(id, null, null);
    }

    private Boolean groupBuildToFinish(String id) {
        return groupBuildToFinish(id, null, null);
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

    private Boolean groupBuildToFinish(String groupBuildId, EnumSet<BuildStatus> isIn, EnumSet<BuildStatus> isNotIn) {
        if (isIn == null)
            isIn = EnumSet.noneOf(BuildStatus.class);
        if (isNotIn == null)
            isNotIn = EnumSet.noneOf(BuildStatus.class);

        GroupBuild build = null;
        logger.debug("Waiting for build {} to finish", groupBuildId);
        try {
            build = groupBuildClient.getSpecific(groupBuildId);
            assertThat(build).isNotNull();
            logger.debug("Gotten build with status: {}", build.getStatus());
            if (!build.getStatus().isFinal())
                return false;
        } catch (RemoteResourceNotFoundException e) {
            fail(String.format("Group Build with id:%s not present", groupBuildId), e);
        } catch (ClientException e) {
            fail("Client has failed in an unexpected way.", e);
        }
        assertThat(build.getStatus()).isNotIn(isNotIn).isIn(isIn);
        return true;
    }
}
