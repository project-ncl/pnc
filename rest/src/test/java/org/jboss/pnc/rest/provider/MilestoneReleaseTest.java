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
package org.jboss.pnc.rest.provider;

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.restmodel.causeway.ArtifactImportError;
import org.jboss.pnc.rest.restmodel.causeway.BuildImportResultRest;
import org.jboss.pnc.rest.restmodel.causeway.BuildImportStatus;
import org.jboss.pnc.rest.restmodel.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.rest.restmodel.causeway.ReleaseStatus;
import org.jboss.pnc.rest.utils.mock.BpmPushMock;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.RandomUtils.randInt;
import static org.jboss.pnc.rest.provider.MilestoneTestUtils.createBuildRecord;
import static org.jboss.pnc.rest.utils.RequestUtils.requestWithEntity;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/25/16
 * Time: 3:21 PM
 */
public class MilestoneReleaseTest extends AbstractMilestoneReleaseTest {
    @Test
    public void shouldNotTriggerReleaseWoEndDate() {
        ProductMilestone milestone = MilestoneTestUtils.prepareMilestone(productMilestoneRepository);
        assertLog(milestone).isNullOrEmpty();
    }

    @Test
    public void shouldProcessSuccessfulCallback() throws Exception {
        BuildRecord buildRecord = createBuildRecord(buildRecordRepository);
        ProductMilestone milestone = createAndReleaseMilestone();
        int taskId = assertBpmCalled(milestone);
        MilestoneReleaseResultRest pushResult =
                createSuccessfulPushResult(milestone, singletonList(createSuccessfulBuildImportResult(buildRecord.getId())));
        releaseBpmCallback(pushResult, taskId);
        assertLog(milestone).contains("Brew push SUCCEEDED");
    }

    @Test
    public void shouldDescribeImportedBuilds() throws Exception {
        ProductMilestone milestone = createAndReleaseMilestone();
        int taskId = assertBpmCalled(milestone);

        BuildRecord record1 = createBuildRecord(buildRecordRepository);
        BuildRecord record2 = createBuildRecord(buildRecordRepository);
        BuildImportResultRest buildResult1 = createSuccessfulBuildImportResult(record1.getId());
        BuildImportResultRest buildResult2 = createSuccessfulBuildImportResult(record2.getId());

        MilestoneReleaseResultRest pushResult =
                createSuccessfulPushResult(milestone, asList(buildResult1, buildResult2));
        releaseBpmCallback(pushResult, taskId);

        assertLog(milestone).contains("Brew push SUCCEEDED")
                .contains(expectedDescription(record1, buildResult1))
                .contains(expectedDescription(record2, buildResult2));
    }

    @Test
    public void shouldDescribeImportErrors() throws Exception {
        ProductMilestone milestone = createAndReleaseMilestone();
        String errorMessage = randomAlphabetic(30);
        int taskId = assertBpmCalled(milestone);

        BuildRecord record = createBuildRecord(buildRecordRepository);
        String artifact1 = "org.jboss:artifact1.pom";
        String artifact2 = "org.jboss:artifact2:1.0.0.GA:jar";

        ArtifactImportError artifactError1 = createArtifactImportError(artifact1);
        ArtifactImportError artifactError2 = createArtifactImportError(artifact2);
        BuildImportResultRest buildResult = createBuildResult(record.getId(),
                BuildImportStatus.FAILED,
                errorMessage,
                asList(artifactError1, artifactError2));

        MilestoneReleaseResultRest pushResult =
                createPushResult(milestone, ReleaseStatus.IMPORT_ERROR,singletonList(buildResult));
        releaseBpmCallback(pushResult, taskId);

        assertLog(milestone).contains("Brew push FAILED")
                .contains("Error message: " + errorMessage)
                .contains(String.format("Failed to import %s [artifactId:%d]. Error message: %s",
                        artifact1, artifactError1.getArtifactId(), artifactError1.getErrorMessage()))
                .contains(String.format("Failed to import %s [artifactId:%d]. Error message: %s",
                        artifact2, artifactError2.getArtifactId(), artifactError2.getErrorMessage()))
                .contains(expectedDescription(record, buildResult));
    }

    private ArtifactImportError createArtifactImportError(String artifactName) {
        int artifactId = randInt(1000, 1000000);

        Artifact artifact = Artifact.Builder.newBuilder().id(artifactId).identifier(artifactName)
                .build();
        artifactRepository.save(artifact);

        ArtifactImportError artifactImportError = new ArtifactImportError();
        artifactImportError.setErrorMessage(randomAlphabetic(30));
        artifactImportError.setArtifactId(artifactId);
        return artifactImportError;
    }

    private String expectedDescription(BuildRecord buildRecord, BuildImportResultRest buildResult) {

        return String.format("%s [buildRecordId: %d, built from %s revision %s tag %s] import %s. Brew build id: %d, Brew build url: %s\n",
                buildRecord.getBuildConfigurationAudited().getBuildConfiguration().getName(),
                buildRecord.getId(),
                buildRecord.getScmRepoURL(),
                buildRecord.getScmRevision(),
                buildRecord.getScmTag(),
                buildResult.getStatus(),
                buildResult.getBrewBuildId(),
                buildResult.getBrewBuildUrl());
    }

    private MilestoneReleaseResultRest createSuccessfulPushResult(ProductMilestone milestone,
                                                                  List<BuildImportResultRest> buildImportResults) {
        return createPushResult(milestone, ReleaseStatus.SUCCESS, buildImportResults);
    }
    private MilestoneReleaseResultRest createPushResult(ProductMilestone milestone,
                                                                  ReleaseStatus status,
                                                                  List<BuildImportResultRest> buildImportResults) {
        MilestoneReleaseResultRest result = new MilestoneReleaseResultRest();
        result.setBuilds(buildImportResults);
        result.setMilestoneId(milestone.getId());
        result.setReleaseStatus(status);
        return result;
    }

    private BuildImportResultRest createSuccessfulBuildImportResult(int buildRecordId) {
        return createBuildResult(buildRecordId, BuildImportStatus.SUCCESSFUL, null, null);
    }

    private BuildImportResultRest createBuildResult(int buildRecordId,
                                                    BuildImportStatus status,
                                                    String errorMessage,
                                                    List<ArtifactImportError> artifactImportErrors) {
        BuildImportResultRest importResult = new BuildImportResultRest();
        importResult.setBrewBuildId(randInt(10000, 200000));
        importResult.setBrewBuildUrl(String.format("http://broo.redhat.com/bild/%d", importResult.getBrewBuildId()));
        importResult.setStatus(status);
        importResult.setBuildRecordId(buildRecordId);
        importResult.setErrorMessage(errorMessage);
        importResult.setErrors(artifactImportErrors);

        return importResult;
    }

    private ProductMilestone createAndReleaseMilestone() throws Exception {
        ProductMilestone milestone = MilestoneTestUtils.prepareMilestone(productMilestoneRepository);

        triggerMilestoneRelease(milestone);

        assertLog(milestone).contains("Brew push task started");
        return milestone;
    }

    private void releaseBpmCallback(MilestoneReleaseResultRest result, Integer taskId) throws CoreException, IOException {
        bpmEndpoint.notifyTask(requestWithEntity(result), taskId);
    }

    private Integer assertBpmCalled(ProductMilestone milestone) {
        Response pushesFor = bpmMock.getPushesFor(milestone.getId());
        BpmPushMock.PushList pushList = (BpmPushMock.PushList) pushesFor.getEntity();

        List<BpmPushMock.Push> pushes = pushList.getPushes();
        assertThat(pushes).hasSize(1);
        BpmPushMock.Push push = pushes.iterator().next();
        return push.getTaskId();
    }

    private AbstractCharSequenceAssert<?, String> assertLog(ProductMilestone milestone) {
        ProductMilestone productMilestone = productMilestoneRepository.queryById(milestone.getId());
        ProductMilestoneRelease release = releaseRepository.findLatestByMilestone(productMilestone);
        return assertThat(release == null ? null : release.getLog());
    }

    private void triggerMilestoneRelease(ProductMilestone milestone) throws RestValidationException {
        ProductMilestoneRest restEntity = new ProductMilestoneRest(milestone);
        milestoneEndpoint.closeMilestone(milestone.getId(), restEntity, null);
    }

}
