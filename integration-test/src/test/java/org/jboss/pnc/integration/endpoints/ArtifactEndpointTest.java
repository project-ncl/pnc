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

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ArtifactClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ArtifactEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpointTest.class);

    private TargetRepository targetRepositoryRef;

    private static Artifact artifactRest1;
    private static Artifact artifactRest2;
    private static Artifact artifactRest3;
    private static Artifact artifactRest4;

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Before
    public void setTargetRepository() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact artifact : client.getAll(null, null, null)) {
            artifacts.add(artifact);
        }

        targetRepositoryRef = artifacts.get(0).getTargetRepository();
        artifactRest1 = artifacts.get(0);
        artifactRest2 = artifacts.get(1);
        artifactRest3 = artifacts.get(4);
        artifactRest4 = artifacts.get(6);
        logger.debug("Using targetRepositoryRef: {}", targetRepositoryRef);
    }

    @Test
    public void testGetAllArtifacts() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> all = client.getAll(null, null, null);

        assertThat(all).hasSize(10); // from DatabaseDataInitializer
    }

    @Test
    public void testGetAllArtifactsFilteredByIdentifier() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<ArtifactInfo> result;

        result = client.getAllFiltered("*demo:*:jar:*", null, null);
        assertThat(result).allSatisfy(
                a -> assertThat(a.getIdentifier().contains("demo:") && a.getIdentifier().contains(":jar:")));

        result = client.getAllFiltered("demo:built-artifact11:pom:*", null, null);
        assertThat(result).hasSize(1); // from DatabaseDataInitializer

        result = client.getAllFiltered("demo:built-artifact22:jar:1.0", null, null);
        assertThat(result).hasSize(1); // from DatabaseDataInitializer
    }

    @Test
    public void testGetAllArtifactsFilteredByQualitiesList() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<ArtifactInfo> result;

        result = client.getAllFiltered(null, new HashSet<>(Arrays.asList(ArtifactQuality.NEW)), null);
        assertThat(result).allSatisfy(a -> assertThat(a.getArtifactQuality().equals(ArtifactQuality.NEW)));

        result = client.getAllFiltered(
                null,
                new HashSet<>(Arrays.asList(ArtifactQuality.VERIFIED, ArtifactQuality.DELETED)),
                null);
        assertThat(result).hasSize(2); // from DatabaseDataInitializer
    }

    @Test
    public void testGetAllArtifactsFilteredByBuildCategories() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<ArtifactInfo> result;

        result = client.getAllFiltered(null, null, null, new HashSet<>(Arrays.asList(BuildCategory.STANDARD)));
        assertThat(result).allSatisfy(a -> assertThat(a.getBuildCategory().equals(BuildCategory.STANDARD)));

        result = client.getAllFiltered(null, null, null, new HashSet<>(Arrays.asList(BuildCategory.SERVICE)));
        assertThat(result).hasSize(2); // from DatabaseDataInitializer
    }

    @Test
    public void testGetAllArtifactsFilteredByRepoType() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());
        RemoteCollection<ArtifactInfo> result;

        RepositoryType type = RepositoryType.NPM;

        result = client.getAllFiltered(null, null, type);
        assertThat(result).hasSize(2) // from DatabaseDataInitializer
                .allSatisfy(a -> assertThat(a.getRepositoryType().equals(type)));
    }

    @Test
    public void testGetAllArtifactsWithMd5() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, artifactRest1.getMd5(), null);

        // artifacts 1 and 2 have same MD5
        assertThat(artifacts).hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId(), artifactRest2.getId()));
    }

    @Test
    public void testGetAllArtifactsWithSha1() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, null, artifactRest2.getSha1());

        // artifacts 2 and 3 have same SHA1
        assertThat(artifacts).hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest2.getId(), artifactRest4.getId()));
    }

    @Test
    public void testGetAllArtifactsWithSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest1.getSha256(), null, null);

        // artifacts 1 and 3 have same SHA256
        assertThat(artifacts).hasSize(2)
                .allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId(), artifactRest4.getId()));
    }

    @Test
    public void testGetAllArtifactsWithMd5AndSha1() throws RemoteResourceException {

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(null, artifactRest2.getMd5(), artifactRest2.getSha1());

        assertThat(artifacts).hasSize(1).allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest2.getId()));
    }

    @Test
    public void testGetAllArtifactsWithMd5AndSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest1.getSha256(), artifactRest1.getMd5(), null);

        assertThat(artifacts).hasSize(1).allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest1.getId()));
    }

    @Test
    public void testGetAllArtifactsWithSha1AndSha256() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<Artifact> artifacts = client.getAll(artifactRest4.getSha256(), null, artifactRest4.getSha1());

        assertThat(artifacts).hasSize(1).allSatisfy(a -> assertThat(a.getId()).isIn(artifactRest4.getId()));
    }

    @Test
    public void testGetSpecificArtifact() throws ClientException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        Artifact artifact = client.getSpecific(artifactRest1.getId());

        assertThat(artifact.getId()).isEqualTo(artifactRest1.getId());
    }

    @Test
    public void shouldFailToSaveArtifact() {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asUser());
        Artifact artifact = Artifact.builder()
                .filename("builtArtifactInsert.jar")
                .identifier("integration-test:built-artifact-insert:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .buildCategory(BuildCategory.STANDARD)
                .md5("insert-md5-1")
                .sha1("insert-1")
                .sha256("insert-1")
                .build();

        Exception caught = null;
        try {
            client.create(artifact);
        } catch (ClientException e) {
            caught = e;
        }
        Assertions.assertThat(caught).isNotNull();
        Assertions.assertThat(caught.getCause()).isInstanceOf(javax.ws.rs.ForbiddenException.class);
    }

    @Test
    public void shouldSaveArtifact() throws ClientException {
        Artifact artifact = Artifact.builder()
                .artifactQuality(ArtifactQuality.NEW)
                .filename("builtArtifactInsert2.jar")
                .identifier("integration-test:built-artifact-insert2:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .buildCategory(BuildCategory.STANDARD)
                .md5("insert-md5-2")
                .sha1("insert-2")
                .sha256("insert-2")
                .size(10L)
                .build();

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact inserted = client.create(artifact);
        String id = inserted.getId();
        Artifact retrieved = client.getSpecific(id);
        Assertions.assertThat(retrieved.getArtifactQuality()).isEqualTo(ArtifactQuality.NEW);
        Assertions.assertThat(retrieved.getMd5()).isEqualTo("insert-md5-2");
        Assertions.assertThat(retrieved.getSize()).isEqualTo(10L);

        Artifact.Builder builder = inserted.toBuilder();
        builder.artifactQuality(ArtifactQuality.TESTED);
        Artifact update = builder.build();
        client.update(id, update);

        Artifact updated = client.getSpecific(id);
        Assertions.assertThat(updated.getArtifactQuality()).isEqualTo(ArtifactQuality.TESTED);
    }

    @Test
    public void shouldUpdateArtifact() throws ClientException {
        String id = artifactRest1.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact artifact = client.getSpecific(id);
        final long size = artifact.getSize() + 10;
        Artifact updatedArtifact = artifact.toBuilder().size(size).build();
        client.update(id, updatedArtifact);

        Artifact artifact2 = client.getSpecific(id);
        assertThat(artifact2.getSize()).isEqualTo(size);
    }

    @Test
    public void shouldGetBuildThatProducedArtifact() throws RemoteResourceException {
        assertThat(artifactRest1.getBuild()).isNotNull();
    }

    @Test
    public void shouldGetBuildsThatDependsOnArtifact() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asUser());

        RemoteCollection<Build> builds = client.getDependantBuilds(artifactRest3.getId());

        assertThat(builds).hasSize(2);
    }

    @Test
    public void shouldGetMilestonesInfo() throws RemoteResourceException {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());

        RemoteCollection<MilestoneInfo> milestonesInfo = client.getMilestonesInfo(artifactRest3.getId());
        assertThat(milestonesInfo).hasSize(1).first().extracting(MilestoneInfo::isBuilt).isEqualTo(false);

        RemoteCollection<MilestoneInfo> milestonesInfo2 = client.getMilestonesInfo(artifactRest1.getId());
        ObjectAssert<MilestoneInfo> milestone = assertThat(milestonesInfo2).hasSize(1).first();
        milestone.extracting(MilestoneInfo::isBuilt).isEqualTo(true);
        milestone.extracting(MilestoneInfo::getProductName).isEqualTo("Project Newcastle Demo Product");
        milestone.extracting(MilestoneInfo::getMilestoneVersion).isEqualTo("1.0.0.Build1");
    }

    @Test
    public void shouldCreateArtifactRevision() throws ClientException {

        String id = artifactRest1.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Iterator<ArtifactRevision> itOriginal = client.getRevisions(id).iterator();
        int numRevisionsOriginal = client.getRevisions(id).size();
        // given latest revision
        ArtifactRevision lastRevOriginal = itOriginal.next();
        while (itOriginal.hasNext()) {
            ArtifactRevision candidate = itOriginal.next();
            if (candidate.getRev() > lastRevOriginal.getRev()) {
                lastRevOriginal = candidate;
            }
        }

        // Updating an audited property should create a new revision
        Artifact artifact = client.getSpecific(id);
        Artifact updatedArtifact = artifact.toBuilder()
                .artifactQuality(ArtifactQuality.TESTED)
                .qualityLevelReason("Preliminary tests passed")
                .build();
        client.update(id, updatedArtifact);

        Iterator<ArtifactRevision> it = client.getRevisions(id).iterator();
        int numRevisions = client.getRevisions(id).size();
        // given latest revision
        ArtifactRevision lastRev = it.next();
        while (it.hasNext()) {
            ArtifactRevision candidate = it.next();
            if (candidate.getRev() > lastRev.getRev()) {
                lastRev = candidate;
            }
        }

        assertThat(numRevisions).isGreaterThan(numRevisionsOriginal);
        assertThat(lastRev.getRev()).isGreaterThan(lastRevOriginal.getRev());
    }

    @Test
    public void shouldGetArtifactRevision() throws Exception {
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asAnonymous());
        Iterator<ArtifactRevision> itOriginal = client.getRevisions(artifactRest1.getId()).iterator();
        // given latest revision
        ArtifactRevision lastRevOriginal = itOriginal.next();
        while (itOriginal.hasNext()) {
            ArtifactRevision candidate = itOriginal.next();
            if (candidate.getRev() > lastRevOriginal.getRev()) {
                lastRevOriginal = candidate;
            }
        }

        ArtifactRevision revision = client.getRevision(artifactRest1.getId(), lastRevOriginal.getRev());

        assertThat(revision.getId()).isEqualTo(artifactRest1.getId());
        assertThat(revision.getArtifactQuality()).isEqualTo(artifactRest1.getArtifactQuality());
        assertThat(revision.getModificationTime()).isEqualTo(artifactRest1.getModificationTime());
        assertThat(revision.getModificationUser()).isEqualTo(artifactRest1.getModificationUser());
        assertThat(revision.getQualityLevelReason()).isEqualTo(artifactRest1.getQualityLevelReason());
    }

    @Test
    public void shouldNotCreateBuildConfigRevision() throws ClientException {
        String id = artifactRest1.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Iterator<ArtifactRevision> itOriginal = client.getRevisions(id).iterator();
        int numRevisionsOriginal = client.getRevisions(id).size();
        // given latest revision
        ArtifactRevision lastRevOriginal = itOriginal.next();
        while (itOriginal.hasNext()) {
            ArtifactRevision candidate = itOriginal.next();
            if (candidate.getRev() > lastRevOriginal.getRev()) {
                lastRevOriginal = candidate;
            }
        }

        // Updating a not audited property should not create a new revision
        Artifact artifact = client.getSpecific(id);
        Artifact updatedArtifact = artifact.toBuilder().size(1000L).build();
        client.update(id, updatedArtifact);

        Iterator<ArtifactRevision> it = client.getRevisions(id).iterator();
        int numRevisions = client.getRevisions(id).size();
        // given latest revision
        ArtifactRevision lastRev = it.next();
        while (it.hasNext()) {
            ArtifactRevision candidate = it.next();
            if (candidate.getRev() > lastRev.getRev()) {
                lastRev = candidate;
            }
        }

        assertThat(numRevisionsOriginal).isEqualTo(numRevisions);
        assertThat(lastRev.getRev()).isEqualTo(lastRevOriginal.getRev());
    }

    @Test
    public void shouldNotModifyCreationModificationFields() throws ClientException {
        String id = artifactRest3.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        // Updating a not audited property should not create a new revision and should not alter modificationUser and
        // modificationTime. Also, creationTime should never be updated, not creationUser
        Artifact artifact = client.getSpecific(id);
        Artifact updatedArtifact = artifact.toBuilder()
                .modificationTime(Instant.now())
                .md5("md5")
                .creationTime(Instant.now())
                .size(1000L)
                .build();
        client.update(id, updatedArtifact);
        Artifact updatedArtifactDB = client.getSpecific(id);

        assertThat(updatedArtifactDB.getId()).isEqualTo(artifact.getId());
        assertThat(updatedArtifactDB.getMd5()).isEqualTo(updatedArtifact.getMd5());
        assertThat(updatedArtifactDB.getSize()).isEqualTo(updatedArtifact.getSize());
        assertThat(updatedArtifactDB.getCreationTime()).isEqualTo(artifact.getCreationTime());
        assertThat(updatedArtifactDB.getCreationUser()).isEqualTo(artifact.getCreationUser());
        assertThat(updatedArtifactDB.getModificationTime()).isEqualTo(artifact.getModificationTime());
        assertThat(updatedArtifactDB.getCreationTime()).isNotEqualTo(updatedArtifact.getCreationTime());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(updatedArtifact.getModificationTime());
    }

    @Test
    public void shouldModifyModificationFields() throws ClientException {
        String id = artifactRest1.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        // Updating an audited property should create a new revision and should alter modificationUser and
        // modificationTime. But, creationTime should never be updated, not creationUser
        Artifact artifact = client.getSpecific(id);
        Artifact updatedArtifact = artifact.toBuilder()
                .modificationTime(Instant.now())
                .artifactQuality(ArtifactQuality.DEPRECATED)
                .creationTime(Instant.now())
                .size(1000L)
                .build();
        client.update(id, updatedArtifact);
        Artifact updatedArtifactDB = client.getSpecific(id);

        assertThat(updatedArtifactDB.getId()).isEqualTo(artifact.getId());
        assertThat(updatedArtifactDB.getArtifactQuality()).isEqualTo(updatedArtifact.getArtifactQuality());
        assertThat(updatedArtifactDB.getSize()).isEqualTo(updatedArtifact.getSize());
        assertThat(updatedArtifactDB.getCreationTime()).isEqualTo(artifact.getCreationTime());
        assertThat(updatedArtifactDB.getCreationUser()).isEqualTo(artifact.getCreationUser());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(artifact.getModificationTime());
        assertThat(updatedArtifactDB.getCreationTime()).isNotEqualTo(updatedArtifact.getCreationTime());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(updatedArtifact.getModificationTime());
    }

    @Test
    public void shouldModifyQualityLevel() throws ClientException {
        String id = artifactRest4.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asUser());
        String REASON = "This artifact has become old enough";

        Artifact artifact = client.getSpecific(id);
        client.createQualityLevelRevision(id, "DEPrecated", REASON);
        Artifact updatedArtifactDB = client.getSpecific(id);

        assertThat(updatedArtifactDB.getId()).isEqualTo(artifact.getId());
        assertThat(updatedArtifactDB.getArtifactQuality()).isEqualTo(ArtifactQuality.DEPRECATED);
        assertThat(updatedArtifactDB.getQualityLevelReason()).isEqualTo(REASON);
        assertThat(updatedArtifactDB.getCreationTime()).isEqualTo(artifact.getCreationTime());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(artifact.getModificationTime());
        assertThat(updatedArtifactDB.getModificationUser().getUsername()).isEqualTo("demo-user");
    }

    @Test
    public void shouldNotStandardUserModifyUnallowedQualityLevel() throws ClientException {
        String id = artifactRest4.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asUser());
        String REASON = "This artifact has become dangerous";

        assertThatThrownBy(() -> client.createQualityLevelRevision(id, "BLACKLISTED", REASON))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldNotApplyUnknownQualityLevel() throws ClientException {
        String id = artifactRest4.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asUser());
        String REASON = "This artifact will be marked as WHITELISTED";

        assertThatThrownBy(() -> client.createQualityLevelRevision(id, "WHITELISTED", REASON))
                .hasCauseInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldNotModifyBlacklistedQualityLevel() throws ClientException {
        String id = artifactRest3.getId();
        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());
        String REASON = "This artifact has severe CVEs";

        Artifact artifact = client.getSpecific(id);
        client.createQualityLevelRevision(id, "BLACKLISTED", REASON);
        Artifact updatedArtifactDB = client.getSpecific(id);

        assertThat(updatedArtifactDB.getId()).isEqualTo(artifact.getId());
        assertThat(updatedArtifactDB.getArtifactQuality()).isEqualTo(ArtifactQuality.BLACKLISTED);
        assertThat(updatedArtifactDB.getQualityLevelReason()).isEqualTo(REASON);
        assertThat(updatedArtifactDB.getCreationTime()).isEqualTo(artifact.getCreationTime());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(artifact.getModificationTime());
        assertThat(updatedArtifactDB.getModificationUser().getUsername()).isEqualTo("system");

        assertThatThrownBy(() -> client.createQualityLevelRevision(id, "DEPRECATED", REASON))
                .hasCauseInstanceOf(ClientErrorException.class);
    }

    @Test
    public void shouldNotModifyTemporaryQualityLevel() throws ClientException {

        Artifact artifact = Artifact.builder()
                .artifactQuality(ArtifactQuality.TEMPORARY)
                .buildCategory(BuildCategory.STANDARD)
                .filename("temp-builtArtifactInsert2.jar")
                .identifier("integration-test:temp-built-artifact-insert2:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .md5("insert-md5-13")
                .sha1("insert-13")
                .sha256("insert-13")
                .size(13L)
                .build();

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact inserted = client.create(artifact);
        String id = inserted.getId();
        Artifact retrieved = client.getSpecific(id);
        Assertions.assertThat(retrieved.getArtifactQuality()).isEqualTo(ArtifactQuality.TEMPORARY);

        String REASON = "This artifact has severe CVEs";

        assertThatThrownBy(() -> client.createQualityLevelRevision(id, "BLACKLISTED", REASON))
                .hasCauseInstanceOf(ClientErrorException.class);
    }

    @Test
    public void shouldDeleteTemporaryQualityLevel() throws ClientException {

        Artifact artifact = Artifact.builder()
                .artifactQuality(ArtifactQuality.TEMPORARY)
                .buildCategory(BuildCategory.STANDARD)
                .filename("temp-builtArtifactInsert3.jar")
                .identifier("integration-test:temp-built-artifact-insert3:jar:1.0")
                .targetRepository(targetRepositoryRef)
                .md5("insert-md5-131")
                .sha1("insert-131")
                .sha256("insert-131")
                .size(131L)
                .build();

        ArtifactClient client = new ArtifactClient(RestClientConfiguration.asSystem());

        Artifact inserted = client.create(artifact);
        String id = inserted.getId();
        Artifact retrieved = client.getSpecific(id);
        Assertions.assertThat(retrieved.getArtifactQuality()).isEqualTo(ArtifactQuality.TEMPORARY);

        String REASON = "This artifact can be nuked";

        client.createQualityLevelRevision(id, "DELEted", REASON);
        Artifact updatedArtifactDB = client.getSpecific(id);

        assertThat(updatedArtifactDB.getId()).isEqualTo(retrieved.getId());
        assertThat(updatedArtifactDB.getArtifactQuality()).isEqualTo(ArtifactQuality.DELETED);
        assertThat(updatedArtifactDB.getQualityLevelReason()).isEqualTo(REASON);
        assertThat(updatedArtifactDB.getCreationTime()).isEqualTo(retrieved.getCreationTime());
        assertThat(updatedArtifactDB.getModificationTime()).isNotEqualTo(retrieved.getModificationTime());
        assertThat(updatedArtifactDB.getModificationUser().getUsername()).isEqualTo("system");
    }
}
