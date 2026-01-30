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
package org.jboss.pnc.integrationrex.endpoints;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.api.orch.dto.BuildDriverResultRest;
import org.jboss.pnc.api.orch.dto.BuildImport;
import org.jboss.pnc.api.orch.dto.BuildMeta;
import org.jboss.pnc.api.orch.dto.BuildResultRest;
import org.jboss.pnc.api.orch.dto.EnvironmentDriverResultRest;
import org.jboss.pnc.api.orch.dto.IdRev;
import org.jboss.pnc.api.orch.dto.ImportBuildsRequest;
import org.jboss.pnc.api.orch.dto.RepositoryManagerResultRest;
import org.jboss.pnc.api.orch.dto.RepourResultRest;
import org.jboss.pnc.auth.KeycloakClient;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.BuildTaskClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integrationrex.RemoteServices;
import org.jboss.pnc.integrationrex.mock.BPMResultsMock;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.restclient.AdvancedBuildClient;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jboss.pnc.integrationrex.mock.ImportResultsMock.*;
import static org.jboss.pnc.integrationrex.mock.ImportResultsMock.generateBuildImport;
import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.withBearerToken;

@RunAsClient
@RunWith(Arquillian.class)
@Category({ ContainerTest.class })
public class BuildTaskEndpointTest extends RemoteServices {
    private BuildConfigurationClient buildConfigurationClient;

    private BuildClient buildClient;

    private BuildTaskClient taskClient;

    private final List<BuildConfigurationRevision> revisions = new ArrayList<>();

    @Before
    public void setup() throws RemoteResourceException {
        String token = KeycloakClient
                .getAuthTokensBySecret(authServerUrl, keycloakRealm, "test-user", "test-pass", "pnc", "", false)
                .getToken();

        buildClient = new AdvancedBuildClient(withBearerToken(token));
        buildConfigurationClient = new BuildConfigurationClient(withBearerToken(token));
        taskClient = new BuildTaskClient(withBearerToken(token));

        buildConfigurationClient.getAll(Optional.of("=desc=name"), Optional.empty()).forEach(bc -> {
            try {
                revisions.add(buildConfigurationClient.getRevisions(bc.getId()).iterator().next());
            } catch (RemoteResourceException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void shouldRejectResultWithoutUniqueAttribute() {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        String random = UUID.randomUUID().toString();
        BuildImport build = generateBuildImport(bcrev, status, Map.of("not-unique", random));
        var request = ImportBuildsRequest.builder().imports(List.of(build)).build();

        // when + then
        assertThatThrownBy(() -> taskClient.importBuilds(request, Set.of("not-in-build")))
                .isInstanceOf(RemoteResourceException.class)
                .hasMessage("Missing attribute in build result.");
    }

    @Test
    public void shouldRejectResultWithExistingDuplicateAttribute() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        String random = UUID.randomUUID().toString();
        Map<String, String> uniqueAttribute = Map.of("unique", random);

        BuildImport build = generateBuildImport(bcrev, status, uniqueAttribute);
        var persist = ImportBuildsRequest.builder().imports(List.of(build)).build();

        IdRev bcrev2 = idRev(1);
        BuildImport build2 = generateBuildImport(bcrev2, status, uniqueAttribute);
        var duplicate = ImportBuildsRequest.builder().imports(List.of(build2)).build();

        // when #1
        List<Build> builds = taskClient.importBuilds(persist, uniqueAttribute.keySet());

        assertThat(builds).hasSize(1);

        Build record = builds.get(0);
        assertThat(record.getId()).isNotNull().isNotBlank();
        assertThat(record.getBuildConfigRevision().getId()).isEqualTo(bcrev.getId().toString());
        assertThat(record.getBuildConfigRevision().getRev()).isEqualTo(bcrev.getRev());
        assertThat(record.getAttributes()).containsAllEntriesOf(uniqueAttribute);

        // when #2 +then
        assertThatThrownBy(() -> taskClient.importBuilds(duplicate, uniqueAttribute.keySet()))
                .isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("Found records with same unique attributes");
    }

    @Test
    public void shouldImportBuildWithValidValues() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        BuildImport importDto = generateBuildImport(bcrev, status, Map.of());
        BuildMeta meta = importDto.getMetadata();
        BuildResultRest result = importDto.getResult();

        var request = ImportBuildsRequest.builder().imports(List.of(importDto)).build();

        // when
        List<Build> builds = taskClient.importBuilds(request, null);

        assertThat(builds).hasSize(1);
        Build record = builds.get(0);
        assertThat(record.getId()).isNotNull().isNotBlank();
        assertThat(record.getStatus()).isEqualTo(BuildStatus.SUCCESS);
        assertThat(record.getBuildConfigRevision().getId()).isEqualTo(bcrev.getId().toString());
        assertThat(record.getBuildConfigRevision().getRev()).isEqualTo(bcrev.getRev());
        assertThat(Date.from(record.getEndTime())).isEqualTo(importDto.getEndTime());
        assertThat(Date.from(record.getStartTime())).isEqualTo(importDto.getStartTime());
        assertThat(Date.from(record.getSubmitTime())).isEqualTo(meta.getSubmitTime());

        var builtArtifacts = buildClient.getBuiltArtifacts(record.getId()).getAll();
        var requestBuiltArtifacts = result.getRepositoryManagerResult().getBuiltArtifacts();

        assertThat(builtArtifacts).isNotEmpty();
        compareArtifacts(builtArtifacts, requestBuiltArtifacts);

        var dependencies = buildClient.getDependencyArtifacts(record.getId()).getAll();
        var requestDependencies = result.getRepositoryManagerResult().getDependencies();

        assertThat(dependencies).isNotEmpty();
        compareArtifacts(dependencies, requestDependencies);
    }

    @Test
    public void shouldImportAFailedBuild() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.FAILED;
        BuildMeta meta = generateMeta(bcrev, Instant.now());
        BuildResultRest resultRest = generateFailedResult(status, Map.of("unique", "1234"));
        BuildImport importDto = generateBuildImport(meta, resultRest);

        var request = ImportBuildsRequest.builder().imports(List.of(importDto)).build();

        // when
        List<Build> builds = taskClient.importBuilds(request, null);

        // then
        assertThat(builds).hasSize(1);
        Build record = builds.get(0);
        assertThat(record.getId()).isNotNull().isNotBlank();
        assertThat(record.getStatus()).isEqualTo(BuildStatus.FAILED);
        assertThat(record.getBuildConfigRevision().getId()).isEqualTo(bcrev.getId().toString());
        assertThat(record.getBuildConfigRevision().getRev()).isEqualTo(bcrev.getRev());
        assertThat(Date.from(record.getEndTime())).isEqualTo(importDto.getEndTime());
        assertThat(Date.from(record.getStartTime())).isEqualTo(importDto.getStartTime());
        assertThat(Date.from(record.getSubmitTime())).isEqualTo(meta.getSubmitTime());
    }

    @Test
    public void shouldImportASErrorBuild() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SYSTEM_ERROR;
        BuildMeta meta = generateMeta(bcrev, Instant.now());
        BuildResultRest resultRest = generateFailedResult(status, Map.of("unique", "1234"));
        BuildImport importDto = generateBuildImport(meta, resultRest);

        var request = ImportBuildsRequest.builder().imports(List.of(importDto)).build();

        // when
        List<Build> builds = taskClient.importBuilds(request, null);

        // then
        assertThat(builds).hasSize(1);
        Build record = builds.get(0);
        assertThat(record.getId()).isNotNull().isNotBlank();
        assertThat(record.getStatus()).isEqualTo(BuildStatus.SYSTEM_ERROR);
        assertThat(record.getBuildConfigRevision().getId()).isEqualTo(bcrev.getId().toString());
        assertThat(record.getBuildConfigRevision().getRev()).isEqualTo(bcrev.getRev());
        assertThat(Date.from(record.getEndTime())).isEqualTo(importDto.getEndTime());
        assertThat(Date.from(record.getStartTime())).isEqualTo(importDto.getStartTime());
        assertThat(Date.from(record.getSubmitTime())).isEqualTo(meta.getSubmitTime());
    }

    // region IDEMPOTENCY TESTS
    @Test
    public void shouldReturnTheSameRecordOnSecondAttempt() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        BuildImport importDto = generateBuildImport(bcrev, status, Map.of());

        var request = ImportBuildsRequest.builder().imports(List.of(importDto)).build();

        // when
        List<Build> firstRequest = taskClient.importBuilds(request, null);
        List<Build> secondRequest = taskClient.importBuilds(request, null);

        // then
        Build record = firstRequest.get(0);
        Build idempotentRecord = secondRequest.get(0);
        assertThat(record).isEqualTo(idempotentRecord);

    }

    @Test
    public void shouldDetectDifferenceIfSameRequestIsModified() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        BuildImport importDto = generateBuildImport(bcrev, status, Map.of());

        var request = ImportBuildsRequest.builder().imports(List.of(importDto)).build();

        // when + then
        // save record
        taskClient.importBuilds(request, null);

        // MODIFICATIONS

        // modify main status
        status = CompletionStatus.FAILED;
        BuildImport importDto1 = importDto.toBuilder()
                .result(importDto.getResult().toBuilder().completionStatus(status).build())
                .build();
        ImportBuildsRequest request1 = ImportBuildsRequest.builder().imports(List.of(importDto1)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request1, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("Import is marked as FAILED but the matched record");

        // modify RMR status
        status = CompletionStatus.FAILED;
        RepositoryManagerResultRest rmr = importDto.getResult()
                .getRepositoryManagerResult()
                .toBuilder()
                .completionStatus(status)
                .build();
        BuildResultRest result2 = importDto.getResult().toBuilder().repositoryManagerResult(rmr).build();
        BuildImport importDto2 = importDto.toBuilder().result(result2).build();
        ImportBuildsRequest request2 = ImportBuildsRequest.builder().imports(List.of(importDto2)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request2, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'repositoryManagerResult.completionStatus'");

        // modify BDR status
        var newBStatus = BuildStatus.FAILED;
        BuildDriverResultRest bdr = importDto.getResult()
                .getBuildDriverResult()
                .toBuilder()
                .buildStatus(newBStatus)
                .build();
        BuildResultRest result3 = importDto.getResult().toBuilder().buildDriverResult(bdr).build();
        BuildImport importDto3 = importDto.toBuilder().result(result3).build();
        ImportBuildsRequest request3 = ImportBuildsRequest.builder().imports(List.of(importDto3)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request3, null)).isInstanceOf(RemoteResourceException.class)

                .hasMessageContaining("differs in field 'buildDriverResult.buildStatus'");

        // modify EDR status
        status = CompletionStatus.FAILED;
        EnvironmentDriverResultRest edr = importDto.getResult()
                .getEnvironmentDriverResult()
                .toBuilder()
                .completionStatus(status)
                .build();
        BuildResultRest result4 = importDto.getResult().toBuilder().environmentDriverResult(edr).build();
        BuildImport importDto4 = importDto.toBuilder().result(result4).build();
        ImportBuildsRequest request4 = ImportBuildsRequest.builder().imports(List.of(importDto4)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request4, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'environmentDriverResult.completionStatus'");

        // modify RR status
        status = CompletionStatus.FAILED;
        RepourResultRest rrr = importDto.getResult().getRepourResult().toBuilder().completionStatus(status).build();
        BuildResultRest result5 = importDto.getResult().toBuilder().repourResult(rrr).build();
        BuildImport importDto5 = importDto.toBuilder().result(result5).build();
        ImportBuildsRequest request5 = ImportBuildsRequest.builder().imports(List.of(importDto5)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request5, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'repourResult.completionStatus'");

        // modify one of built artifacts
        List<Artifact> builtArtifacts = new ArrayList<>(
                importDto.getResult().getRepositoryManagerResult().getBuiltArtifacts());
        Artifact artifact = builtArtifacts.stream().findAny().get().toBuilder().filename("change.org").build();
        Artifact changedArtifact = artifact.toBuilder().filename("change.org").build();
        builtArtifacts.remove(artifact);
        builtArtifacts.add(changedArtifact);
        RepositoryManagerResultRest rmr1 = importDto.getResult()
                .getRepositoryManagerResult()
                .toBuilder()
                .builtArtifacts(builtArtifacts)
                .build();
        BuildResultRest result6 = importDto.getResult().toBuilder().repositoryManagerResult(rmr1).build();
        BuildImport importDto6 = importDto.toBuilder().result(result6).build();
        ImportBuildsRequest request6 = ImportBuildsRequest.builder().imports(List.of(importDto6)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request6, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'builtArtifacts'");

        // modify one of dependency artifacts
        List<Artifact> dependencies = new ArrayList<>(
                importDto.getResult().getRepositoryManagerResult().getDependencies());
        Artifact dep = dependencies.stream().findAny().get().toBuilder().filename("change.org").build();
        Artifact changedDep = dep.toBuilder().filename("change.org").build();
        dependencies.remove(dep);
        dependencies.add(changedDep);
        RepositoryManagerResultRest rmr2 = importDto.getResult()
                .getRepositoryManagerResult()
                .toBuilder()
                .dependencies(dependencies)
                .build();
        BuildResultRest result7 = importDto.getResult().toBuilder().repositoryManagerResult(rmr2).build();
        BuildImport importDto7 = importDto.toBuilder().result(result7).build();
        ImportBuildsRequest request7 = ImportBuildsRequest.builder().imports(List.of(importDto7)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request7, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'dependencyArtifacts'");

        // add an arfifact
        List<Artifact> dependencies1 = new ArrayList<>(
                importDto.getResult().getRepositoryManagerResult().getDependencies());
        Artifact mockArtifact = BPMResultsMock.mockArtifact("abcc");
        dependencies1.add(mockArtifact);
        RepositoryManagerResultRest rmr3 = importDto.getResult()
                .getRepositoryManagerResult()
                .toBuilder()
                .dependencies(dependencies1)
                .build();
        BuildResultRest result8 = importDto.getResult().toBuilder().repositoryManagerResult(rmr3).build();
        BuildImport importDto8 = importDto.toBuilder().result(result8).build();
        ImportBuildsRequest request8 = ImportBuildsRequest.builder().imports(List.of(importDto8)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request8, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'dependencyArtifacts'");

        // remove an arfifact
        List<Artifact> dependencies2 = new ArrayList<>(
                importDto.getResult().getRepositoryManagerResult().getDependencies());
        Artifact dep1 = dependencies2.stream().findAny().get();
        dependencies2.remove(dep1);
        RepositoryManagerResultRest rmr4 = importDto.getResult()
                .getRepositoryManagerResult()
                .toBuilder()
                .dependencies(dependencies2)
                .build();
        BuildResultRest result9 = importDto.getResult().toBuilder().repositoryManagerResult(rmr4).build();
        BuildImport importDto9 = importDto.toBuilder().result(result9).build();
        ImportBuildsRequest request9 = ImportBuildsRequest.builder().imports(List.of(importDto9)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request9, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'dependencyArtifacts'");

        // change something in meta
        BuildMeta diffMeta = importDto.getMetadata().toBuilder().contentId("different").build();
        BuildImport importDto10 = importDto.toBuilder().metadata(diffMeta).build();
        ImportBuildsRequest request10 = ImportBuildsRequest.builder().imports(List.of(importDto10)).build();

        assertThatThrownBy(() -> taskClient.importBuilds(request10, null)).isInstanceOf(RemoteResourceException.class)
                .hasMessageContaining("differs in field 'buildContentId'");
    }

    @Test
    public void shouldReturnTheSameRecordIfAlreadySavedWithMultiImport() throws RemoteResourceException {
        // with
        IdRev bcrev = idRev(0);
        CompletionStatus status = CompletionStatus.SUCCESS;
        BuildImport number1 = generateBuildImport(bcrev, status, Map.of());
        var request = ImportBuildsRequest.builder().imports(List.of(number1)).build();
        BuildImport number2 = generateBuildImport(idRev(1), status, Map.of());
        var request2 = ImportBuildsRequest.builder().imports(List.of(number1, number2)).build();

        // when
        List<Build> firstRequest = taskClient.importBuilds(request, null);

        List<Build> secondRequest = taskClient.importBuilds(request2, null);

        // then
        Build record = firstRequest.get(0);
        assertThat(secondRequest).contains(record);

        secondRequest.remove(record);
        Build secondRecord = secondRequest.get(0);
        assertThat(secondRecord.getId()).isNotNull().isNotEmpty();
    }
    // endregion

    private IdRev idRev(int index) {
        return IdRev.builder()
                .id(Integer.valueOf(revisions.get(index).getId()))
                .rev(revisions.get(index).getRev())
                .build();
    }

    public static void compareArtifacts(Collection<Artifact> artifacts, Collection<Artifact> requestArtifacts) {
        assertThatCollection(artifacts).usingRecursiveFieldByFieldElementComparatorOnFields(
                "identifier",
                "purl",
                "size",
                "artifactQuality",
                "md5",
                "sha1",
                "sha256",
                "filename",
                "deployPath",
                "importDate",
                "originUrl").isEqualTo(requestArtifacts);

        var targetRepositories = artifacts.stream().map(Artifact::getTargetRepository).collect(Collectors.toList());
        var requestTargetRepositories = requestArtifacts.stream()
                .map(Artifact::getTargetRepository)
                .collect(Collectors.toList());
        assertThatCollection(targetRepositories).usingRecursiveFieldByFieldElementComparatorOnFields(
                "temporaryRepo",
                "identifier",
                "repositoryType",
                "repositoryPath").isEqualTo(requestTargetRepositories);
    }
}
