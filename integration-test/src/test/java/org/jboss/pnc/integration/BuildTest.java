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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetWithAuditedBCsRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.RebuildMode;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.registerCustomDateFormat;
import static org.jboss.pnc.integration.deployments.Deployments.addBuildExecutorMock;


/**
 * The tests are not running the actual builds it uses BuildExecutorMock
 */
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

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems(){
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.firstNotNull().getValue();

        //when
        RestResponse<BuildRecordRest> triggeredConfiguration = triggerBCBuild(buildConfiguration, Optional.empty());

        //then
        verifySingleBuild(triggeredConfiguration);
    }

    @Test
    public void shouldTriggerBuildWithADependencyAndFinishWithoutProblems() {
        //given - A BC with a dependency on pnc-1.0.0.DR1
        BuildConfigurationRest buildConfigurationParent = buildConfigurationRestClient.getByName("dependency-analysis-1.3").getValue();

        // Update dependency
        BuildConfigurationRest buildConfigurationChild = buildConfigurationRestClient.getByName("pnc-1.0.0.DR1").getValue();
        BuildConfigurationRest updatedBuildConfigurationChild = updateBCDescription(buildConfigurationChild, buildConfigurationChild.getDescription() + ".");

        // The update of the description should not have changed the lastModificationDate
        assertThat(buildConfigurationChild.getLastModificationTime()).isEqualTo(updatedBuildConfigurationChild.getLastModificationTime());

        //when
        RestResponse<BuildRecordRest> triggeredConfiguration = triggerBCBuild(buildConfigurationParent, Optional.empty());

        //then
        assertThat(triggeredConfiguration.getRestCallResponse().getStatusCode()).isEqualTo(200);

        BuildRecordRest buildResponse = triggeredConfiguration.getValue();
        assertThat(buildResponse.getDependencyBuildRecordIds().length).isEqualTo(1);

        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildResponse.getId(), false).hasValue(), 15, TimeUnit.SECONDS);
        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildResponse.getDependencyBuildRecordIds()[0], false).hasValue(), 15, TimeUnit.SECONDS);
    }

    private RestResponse<BuildRecordRest> triggerBCBuild(BuildConfigurationRest buildConfiguration, Optional<Integer> revision) {
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);

        return triggerBCBuild(buildConfiguration, revision, buildOptions);
    }

    private RestResponse<BuildRecordRest> triggerBCBuild(BuildConfigurationRest buildConfiguration, Optional<Integer> revision, BuildOptions buildOptions) {
        RestResponse<UserRest> loggedUser = userRestClient.getLoggedUser();

        logger.debug("LoggedUser: {}", loggedUser.hasValue() ? loggedUser.getValue() : "-no-logged-user-");
        logger.info("About to trigger build: {} with id: {}.", buildConfiguration.getName(), buildConfiguration.getId());

        RestResponse<BuildRecordRest> triggeredConfiguration;
        if(revision.isPresent()) {
            triggeredConfiguration = buildConfigurationRestClient.trigger(buildConfiguration.getId(), revision.get(),  buildOptions);
        } else {
            triggeredConfiguration = buildConfigurationRestClient.trigger(buildConfiguration.getId(), buildOptions);
        }
        logger.debug("Response from triggered build: {}.", triggeredConfiguration.hasValue() ? triggeredConfiguration.getValue() : "-response-not-available-");

        return triggeredConfiguration;
    }

    @Test
    public void shouldTriggerBuildWithRevisionAndFinishWithoutProblems() {
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.firstNotNull().getValue();

        //when
        RestResponse<BuildRecordRest> triggeredConfiguration = triggerBCBuild(buildConfiguration, Optional.of(1));

        //then
        verifySingleBuild(triggeredConfiguration);
    }

    private void verifySingleBuild(RestResponse<BuildRecordRest> triggeredConfiguration) {
        Integer buildRecordId = ResponseUtils.getIdFromLocationHeader(triggeredConfiguration.getRestCallResponse());
        logger.info("New build record id: {}.", buildRecordId);
        assertThat(triggeredConfiguration.getRestCallResponse().getStatusCode()).isEqualTo(200);

        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildRecordId, false).hasValue(), 15, TimeUnit.SECONDS);
    }

    @Test
    public void shouldTriggerBuildSetAndFinishWithoutProblems() {
        //given
        BuildConfigurationSetRest buildConfigurationSet = buildConfigurationSetRestClient.firstNotNull().getValue();

        //when
        userRestClient.getLoggedUser(); //initialize user
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);

        RestResponse<BuildConfigSetRecordRest> response = buildConfigurationSetRestClient.trigger(buildConfigurationSet.getId(), buildOptions);
        Integer buildRecordSetId = response.getValue().getId();

        //then
        verifyBuildSetResults(response, buildRecordSetId);
    }

    @Test
    public void shouldTriggerBuildSetWithBCInRevisionAndFinishWithoutProblems() {
        //given
        BuildConfigurationSetRest buildConfigurationSet = buildConfigurationSetRestClient.firstNotNull().getValue();
        assertThat(buildConfigurationSet.getBuildConfigurationIds().size()).isGreaterThan(0);

        List<BuildConfigurationAuditedRest> buildConfigurationAuditedRestList = new ArrayList<>();
        BuildConfigurationAuditedRest buildConfigurationAuditedRest = new BuildConfigurationAuditedRest();
        buildConfigurationAuditedRest.setId(buildConfigurationSet.getBuildConfigurationIds().get(0));
        buildConfigurationAuditedRest.setRev(1);
        buildConfigurationAuditedRestList.add(buildConfigurationAuditedRest);

        BuildConfigurationSetWithAuditedBCsRest buildConfigurationSetWithAuditedBCsRest = new BuildConfigurationSetWithAuditedBCsRest();
        buildConfigurationSetWithAuditedBCsRest.setId(buildConfigurationSet.getId());
        buildConfigurationSetWithAuditedBCsRest.setBuildConfigurationAuditeds(buildConfigurationAuditedRestList);

        //when
        userRestClient.getLoggedUser(); //initialize user
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);

        RestResponse<BuildConfigSetRecordRest> response = buildConfigurationSetRestClient.trigger(buildConfigurationSet.getId(), buildConfigurationSetWithAuditedBCsRest, buildOptions);
        Integer buildRecordSetId = response.getValue().getId();

        //then
        verifyBuildSetResults(response, buildRecordSetId);
    }

    @Test
    public void shouldBuildTemporaryBuildAndNotAssignItToMilestone() {
        // BC pnc-1.0.0.DR1 is assigned to a product version containing an active product milestone see DatabaseDataInitializer#initiliazeProjectProductData
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.getByName("pnc-1.0.0.DR1").getValue();

        //when
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        buildOptions.setTemporaryBuild(true);
        RestResponse<BuildRecordRest> triggeredConfiguration = triggerBCBuild(buildConfiguration, Optional.empty(), buildOptions);

        //then
        assertThat(triggeredConfiguration.getRestCallResponse().getStatusCode()).isEqualTo(200);

        Integer buildRecordId = triggeredConfiguration.getValue().getId();

        ResponseUtils.waitSynchronouslyFor(() -> buildRecordRestClient.get(buildRecordId, false).hasValue(), 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> buildRecordRestResponse = buildRecordRestClient.get(buildRecordId);
        assertThat(buildRecordRestResponse.getRestCallResponse().getStatusCode()).isEqualTo(200);
        assertThat(buildRecordRestResponse.getValue().getProductMilestoneId()).isNull();
    }

    @Test
    public void shouldNotTriggerANewPersistentBuildWithoutForceIfOnlyDescriptionChanged() {
        BuildConfigurationRest buildConfigurationRest = buildConfigurationRestClient.getByName("maven-plugin-test").getValue();
        BuildOptions persistent = new BuildOptions();
        persistent.setRebuildMode(RebuildMode.FORCE);

        // Trigger force build
        RestResponse<BuildRecordRest> forcedPersistentBuild = triggerBCBuild(buildConfigurationRest, Optional.empty(), persistent);
        assertThat(forcedPersistentBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(forcedPersistentBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        } , 15, TimeUnit.SECONDS);

        // Update only description, should not create a new revision and keep same lastModificationTime
        String oldDescription = buildConfigurationRest.getDescription();
        Date oldLastModDate = buildConfigurationRest.getLastModificationTime();
        BuildConfigurationRest updatedBuildConfigurationRest = updateBCDescription(buildConfigurationRest, "Random Description to be able to trigger build again so that persistent build will be first on this revision");
        assertThat(oldDescription).isNotEqualTo(updatedBuildConfigurationRest.getDescription());
        assertThat(oldLastModDate).isEqualTo(updatedBuildConfigurationRest.getLastModificationTime());

        // Trigger a new build without force, should not build again
        persistent = new BuildOptions();
        RestResponse<BuildRecordRest> buildRecord = triggerBCBuild(updatedBuildConfigurationRest, Optional.empty(), persistent);
        assertThat(buildRecord.getRestCallResponse().getStatusCode()).isEqualTo(200);

        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(buildRecord.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
        }, 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> response = buildRecordRestClient.get(buildRecord.getValue().getId());
        assertThat(response.getValue().getStatus()).isEqualTo(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
    }

    @Test
    public void shouldNotTriggerANewTemporaryBuildWithoutForceIfOnlyDescriptionChanged() {
        BuildConfigurationRest buildConfigurationRest = buildConfigurationRestClient.getByName("maven-plugin-test").getValue();
        BuildOptions temporary = new BuildOptions();
        temporary.setRebuildMode(RebuildMode.FORCE);
        temporary.setTemporaryBuild(true);

        // Trigger force build
        RestResponse<BuildRecordRest> forcedTemporaryBuild = triggerBCBuild(buildConfigurationRest, Optional.empty(), temporary);
        assertThat(forcedTemporaryBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(forcedTemporaryBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        } , 15, TimeUnit.SECONDS);

        // Update only description, should not create a new revision and keep same lastModificationTime
        String oldDescription = buildConfigurationRest.getDescription();
        Date oldLastModDate = buildConfigurationRest.getLastModificationTime();
        BuildConfigurationRest updatedBuildConfigurationRest = updateBCDescription(buildConfigurationRest, "Random Description to be able to trigger build again so that persistent build will be first on this revision");
        assertThat(oldDescription).isNotEqualTo(updatedBuildConfigurationRest.getDescription());
        assertThat(oldLastModDate).isEqualTo(updatedBuildConfigurationRest.getLastModificationTime());

        // Trigger a new build without force, should not build again
        temporary = new BuildOptions();
        temporary.setTemporaryBuild(true);
        RestResponse<BuildRecordRest> buildRecord = triggerBCBuild(updatedBuildConfigurationRest, Optional.empty(), temporary);
        assertThat(buildRecord.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(buildRecord.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
        }, 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> response = buildRecordRestClient.get(buildRecord.getValue().getId());
        assertThat(response.getValue().getStatus()).isEqualTo(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
    }

    @Test
    public void shouldTriggerPersistentAfterSingleTemporaryWithoutForce() {
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.getByName("maven-plugin-test").getValue();
        BuildOptions persistent = new BuildOptions();
        BuildOptions temporary = new BuildOptions();
        temporary.setTemporaryBuild(true);

        // Updating the description only won't create a new revision, as description is not audited anymore
        Date oldLastModDate = buildConfiguration.getLastModificationTime();
        buildConfiguration.setBuildScript("mvn" + " clean deploy -DskipTests=true");
        BuildConfigurationRest updatedBuildConfiguration = updateBCDescription(buildConfiguration, "Updating the description only will not create a new revision, as description is not audited anymore");
        assertThat(oldLastModDate).isNotEqualTo(updatedBuildConfiguration.getLastModificationTime());

        RestResponse<BuildRecordRest> temporaryBuild = triggerBCBuild(updatedBuildConfiguration,Optional.empty(),temporary);
        assertThat(temporaryBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(temporaryBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        } , 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> afterTempPersistentBuild = triggerBCBuild(updatedBuildConfiguration,Optional.empty(),persistent);
        assertThat(afterTempPersistentBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(afterTempPersistentBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        }, 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> finalRecord = buildRecordRestClient.get(afterTempPersistentBuild.getValue().getId());
        assertThat(finalRecord.getValue().getStatus()).isNotIn(BuildCoordinationStatus.REJECTED_ALREADY_BUILT, BuildCoordinationStatus.REJECTED);
    }

    //NCL-5192
    //Replicates NCL-5192 through explicit dependency instead of implicit
    @Test
    public void dontRebuildTemporaryBuildWhenThereIsNewerPersistentOnSameRev() {
        BuildConfigurationRest parent = buildConfigurationRestClient.getByName("pnc-build-agent-0.4").getValue();
        BuildConfigurationRest dependency = buildConfigurationRestClient.getByName("termd").getValue();

        BuildOptions persistent = new BuildOptions();
        BuildOptions temporary = new BuildOptions();
        temporary.setTemporaryBuild(true);
        // Updating the description only won't create a new revision, as description is not audited anymore
        parent.setBuildScript("mvn" + "  clean deploy -DskipTests=true");
        BuildConfigurationRest updatedParent = updateBCDescription(parent, "Updating the description only will not create a new revision, as description is not audited anymore");
        assertThat(parent.getLastModificationTime()).isNotEqualTo(updatedParent.getLastModificationTime());

        // Updating the description only won't create a new revision, as description is not audited anymore
        dependency.setBuildScript("mvn" + "  clean deploy -DskipTests=true");
        BuildConfigurationRest updatedDependency = updateBCDescription(dependency, "Updating the description only will not create a new revision, as description is not audited anymore");
        assertThat(dependency.getLastModificationTime()).isNotEqualTo(updatedDependency.getLastModificationTime());

        //Build temporary builds (parent and dependency) on new revision
        RestResponse<BuildRecordRest> temporaryBuild = triggerBCBuild(updatedParent,Optional.empty(),temporary);
        assertThat(temporaryBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(temporaryBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        } , 15, TimeUnit.SECONDS);

        //Build persistent build of dependency on the same revision
        RestResponse<BuildRecordRest> dependencyPersistentBuild = triggerBCBuild(updatedDependency,Optional.empty(),persistent);
        assertThat(dependencyPersistentBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(dependencyPersistentBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        }, 15, TimeUnit.SECONDS);

        //Build temporary build of parent and check it gets REJECTED even if it's dependency has newer record
        //(in this case temp build should ignore persistent one)
        RestResponse<BuildRecordRest> finalRecord = triggerBCBuild(updatedParent, Optional.empty(), temporary);
        assertThat(finalRecord.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(finalRecord.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
        }, 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> response = buildRecordRestClient.get(finalRecord.getValue().getId());
        assertThat(response.getValue().getStatus()).isEqualTo(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
    }

    @Test
    public void shouldRejectAfterBuildingTwoTempBuildsOnSameRevision() {
        BuildConfigurationRest buildConfiguration = buildConfigurationRestClient.getByName("maven-plugin-test").getValue();
        BuildOptions temporary = new BuildOptions();
        temporary.setTemporaryBuild(true);
        BuildOptions temporary2 = new BuildOptions();
        temporary2.setTemporaryBuild(true);

        buildConfiguration.setBuildScript("mvn" + "   clean deploy -DskipTests=true");
        BuildConfigurationRest updatedBuildConfiguration = updateBCDescription(buildConfiguration, buildConfiguration.getDescription());
        assertThat(buildConfiguration.getLastModificationTime()).isNotEqualTo(updatedBuildConfiguration.getLastModificationTime());

        RestResponse<BuildRecordRest> temporaryBuild = triggerBCBuild(updatedBuildConfiguration,Optional.empty(),temporary);
        assertThat(temporaryBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(temporaryBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.DONE);
        } , 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> secondTempBuild = triggerBCBuild(updatedBuildConfiguration,Optional.empty(),temporary2);
        assertThat(secondTempBuild.getRestCallResponse().getStatusCode()).isEqualTo(200);
        ResponseUtils.waitSynchronouslyFor(() -> {
            RestResponse<BuildRecordRest> record = buildRecordRestClient.get(secondTempBuild.getValue().getId(),
                    false);
            return record.hasValue() && record.getValue().getStatus().equals(BuildCoordinationStatus.REJECTED_ALREADY_BUILT);
        }, 15, TimeUnit.SECONDS);

        RestResponse<BuildRecordRest> finalRecord = buildRecordRestClient.get(secondTempBuild.getValue().getId());
        assertThat(finalRecord.getValue().getStatus()).isNotIn(BuildCoordinationStatus.DONE, BuildCoordinationStatus.REJECTED);
    }

    private void verifyBuildSetResults(RestResponse<BuildConfigSetRecordRest> response, Integer buildRecordSetId) {
        assertThat(response.getRestCallResponse().getStatusCode()).isEqualTo(200);
        assertThat(buildRecordSetId).isNotNull();

        ResponseUtils.waitSynchronouslyFor(() -> buildConfigSetRecordRestClient.get(buildRecordSetId, false).hasValue(), 15, TimeUnit.SECONDS);
        assertThat(buildConfigSetRecordRestClient.get(buildRecordSetId, false).getValue().getStatus())
                .isNotEqualTo(BuildStatus.REJECTED);
    }

    // This custom method is done so to make it visible that the update of only the decription should not cause a new revision to be created, nor the lastModificationDate to be changed
    private BuildConfigurationRest updateBCDescription(BuildConfigurationRest buildConfigurationRest, String description) {
        buildConfigurationRest.setDescription(description);
        RestResponse<BuildConfigurationRest> updatedBuildConfigurationChild = buildConfigurationRestClient.update(buildConfigurationRest.getId(), buildConfigurationRest);
        return updatedBuildConfigurationChild.getValue();
    }
}
