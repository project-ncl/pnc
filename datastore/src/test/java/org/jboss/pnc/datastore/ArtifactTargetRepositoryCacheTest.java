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
package org.jboss.pnc.datastore;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for artifact caching with target repository uniqueness.
 *
 * Tests verify that: 1. Artifacts with same identifier+sha256 but different target repositories are treated as distinct
 * 2. Artifacts appearing in both built and dependencies are cached correctly 3. The predicate
 * withIdentifierAndSha256AndTargetRepository correctly filters by repository
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ArtifactTargetRepositoryCacheTest {

    @Inject
    Datastore datastore;

    @Inject
    ArtifactRepository artifactRepository;

    @Inject
    TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    UserRepository userRepository;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    private TargetRepository mavenRepo;
    private TargetRepository npmRepo;
    private User testUser;
    private BuildConfigurationAudited buildConfigAud;

    @Before
    @Transactional
    public void setup() {
        // Create two different target repositories
        mavenRepo = TargetRepository.newBuilder()
                .repositoryType(RepositoryType.MAVEN)
                .repositoryPath("maven-releases")
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .temporaryRepo(false)
                .build();
        mavenRepo = targetRepositoryRepository.save(mavenRepo);

        npmRepo = TargetRepository.newBuilder()
                .repositoryType(RepositoryType.NPM)
                .repositoryPath("npm-releases")
                .identifier(ReposiotryIdentifier.INDY_NPM)
                .temporaryRepo(false)
                .build();
        npmRepo = targetRepositoryRepository.save(npmRepo);

        // Create test user
        testUser = User.Builder.newBuilder().username("test-user").email("test@example.com").build();
        testUser = userRepository.save(testUser);

        // Get a build configuration audited
        List<BuildConfiguration> buildConfigs = buildConfigurationRepository.queryAll();
        if (buildConfigs.isEmpty()) {
            throw new IllegalStateException("No BuildConfiguration available for testing");
        }
        BuildConfiguration buildConfig = buildConfigs.get(0);
        List<BuildConfigurationAudited> buildConfigAudList = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(buildConfig.getId());
        if (buildConfigAudList.isEmpty()) {
            throw new IllegalStateException("No BuildConfigurationAudited available for testing");
        }
        buildConfigAud = buildConfigAudList.get(0);
    }

    /**
     * Test that artifacts with the same identifier and sha256 but different target repositories are treated as distinct
     * artifacts.
     */
    @Test
    @Transactional
    public void testSameArtifactDifferentRepositories() throws Exception {
        String identifier = "org.example:lib:1.0.0";
        String sha256 = "abc123def456";

        // Create same artifact in two different repositories
        Artifact mavenArtifact = Artifact.Builder.newBuilder()
                .identifier(identifier)
                .md5("md5-" + sha256)
                .sha1("sha1-" + sha256)
                .sha256(sha256)
                .size(1000L)
                .targetRepository(mavenRepo)
                .build();

        Artifact npmArtifact = Artifact.Builder.newBuilder()
                .identifier(identifier)
                .md5("md5-" + sha256)
                .sha1("sha1-" + sha256)
                .sha256(sha256)
                .size(1000L)
                .targetRepository(npmRepo)
                .build();

        List<Artifact> builtArtifacts = new ArrayList<>();
        builtArtifacts.add(mavenArtifact);
        builtArtifacts.add(npmArtifact);

        BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder()
                .id(Sequence.nextBase32Id())
                .buildConfigurationAudited(buildConfigAud)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(testUser)
                .temporaryBuild(false);

        BuildRecord savedRecord = datastore
                .storeCompletedBuild(buildRecordBuilder, builtArtifacts, new ArrayList<>(), new ArrayList<>());

        // Verify both artifacts were saved as distinct entities
        assertThat(savedRecord.getBuiltArtifacts()).hasSize(2);

        // Verify we can query them separately by repository
        Set<Artifact.IdentifierSha256TargetRepository> mavenConstraints = new HashSet<>();
        mavenConstraints.add(new Artifact.IdentifierSha256TargetRepository(identifier, sha256, mavenRepo.getId()));

        Set<Artifact> mavenResults = artifactRepository.withIdentifierAndSha256AndTargetRepository(mavenConstraints);
        assertThat(mavenResults).hasSize(1);
        assertThat(mavenResults.iterator().next().getTargetRepository().getId()).isEqualTo(mavenRepo.getId());

        Set<Artifact.IdentifierSha256TargetRepository> npmConstraints = new HashSet<>();
        npmConstraints.add(new Artifact.IdentifierSha256TargetRepository(identifier, sha256, npmRepo.getId()));

        Set<Artifact> npmResults = artifactRepository.withIdentifierAndSha256AndTargetRepository(npmConstraints);
        assertThat(npmResults).hasSize(1);
        assertThat(npmResults.iterator().next().getTargetRepository().getId()).isEqualTo(npmRepo.getId());
    }

    /**
     * Test that when the same artifact appears in both builtArtifacts and dependencies, it is properly cached and not
     * queried twice from the database.
     */
    @Test
    @Transactional
    public void testArtifactCacheSharedBetweenBuiltAndDependencies() throws Exception {
        String identifier = "org.example:shared-lib:2.0.0";
        String sha256 = "shared-sha256";

        // Create an artifact that will appear in both lists
        Artifact sharedArtifact = Artifact.Builder.newBuilder()
                .identifier(identifier)
                .md5("md5-" + sha256)
                .sha1("sha1-" + sha256)
                .sha256(sha256)
                .size(2000L)
                .targetRepository(mavenRepo)
                .build();

        List<Artifact> builtArtifacts = new ArrayList<>();
        builtArtifacts.add(sharedArtifact);

        List<Artifact> dependencies = new ArrayList<>();
        dependencies.add(sharedArtifact);

        BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder()
                .id(Sequence.nextBase32Id())
                .buildConfigurationAudited(buildConfigAud)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(testUser)
                .temporaryBuild(false);

        BuildRecord savedRecord = datastore
                .storeCompletedBuild(buildRecordBuilder, builtArtifacts, dependencies, new ArrayList<>());

        // Verify the artifact was saved only once
        assertThat(savedRecord.getBuiltArtifacts()).hasSize(1);
        assertThat(savedRecord.getDependencies()).hasSize(1);

        // Verify only one artifact in database (not duplicated)
        Set<Artifact.IdentifierSha256TargetRepository> constraints = new HashSet<>();
        constraints.add(new Artifact.IdentifierSha256TargetRepository(identifier, sha256, mavenRepo.getId()));

        Set<Artifact> results = artifactRepository.withIdentifierAndSha256AndTargetRepository(constraints);
        assertThat(results).hasSize(1);
    }

    /**
     * Test the IdentifierSha256TargetRepository class equals and hashCode methods.
     */
    @Test
    public void testIdentifierSha256TargetRepositoryEqualsAndHashCode() {
        Artifact.IdentifierSha256TargetRepository key1 = new Artifact.IdentifierSha256TargetRepository(
                "id1",
                "sha1",
                1);
        Artifact.IdentifierSha256TargetRepository key2 = new Artifact.IdentifierSha256TargetRepository(
                "id1",
                "sha1",
                1);
        Artifact.IdentifierSha256TargetRepository key3 = new Artifact.IdentifierSha256TargetRepository(
                "id1",
                "sha1",
                2);
        Artifact.IdentifierSha256TargetRepository key4 = new Artifact.IdentifierSha256TargetRepository(
                "id2",
                "sha1",
                1);

        // Test equals
        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
        assertThat(key1).isNotEqualTo(key4);

        // Test hashCode consistency
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());

        // Test in HashSet (relies on hashCode and equals)
        Set<Artifact.IdentifierSha256TargetRepository> set = new HashSet<>();
        set.add(key1);
        assertThat(set.contains(key2)).isTrue();
        assertThat(set.contains(key3)).isFalse();
    }

    /**
     * Test querying multiple artifacts with different repositories in one query.
     */
    @Test
    @Transactional
    public void testBatchQueryWithMultipleRepositories() throws Exception {
        // Create artifacts across different repositories
        Artifact artifact1 = createAndSaveArtifact("org.test:lib1:1.0", "sha1", mavenRepo);
        Artifact artifact2 = createAndSaveArtifact("org.test:lib2:1.0", "sha2", mavenRepo);
        Artifact artifact3 = createAndSaveArtifact("org.test:lib3:1.0", "sha3", npmRepo);

        // Query for all three at once
        Set<Artifact.IdentifierSha256TargetRepository> constraints = new HashSet<>();
        constraints.add(
                new Artifact.IdentifierSha256TargetRepository(
                        artifact1.getIdentifier(),
                        artifact1.getSha256(),
                        mavenRepo.getId()));
        constraints.add(
                new Artifact.IdentifierSha256TargetRepository(
                        artifact2.getIdentifier(),
                        artifact2.getSha256(),
                        mavenRepo.getId()));
        constraints.add(
                new Artifact.IdentifierSha256TargetRepository(
                        artifact3.getIdentifier(),
                        artifact3.getSha256(),
                        npmRepo.getId()));

        Set<Artifact> results = artifactRepository.withIdentifierAndSha256AndTargetRepository(constraints);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(Artifact::getIdentifier)
                .containsExactlyInAnyOrder("org.test:lib1:1.0", "org.test:lib2:1.0", "org.test:lib3:1.0");
    }

    private Artifact createAndSaveArtifact(String identifier, String sha256, TargetRepository targetRepo)
            throws Exception {
        Artifact artifact = Artifact.Builder.newBuilder()
                .identifier(identifier)
                .md5("md5-" + sha256)
                .sha1("sha1-" + sha256)
                .sha256(sha256)
                .size(1000L)
                .targetRepository(targetRepo)
                .build();

        List<Artifact> artifacts = new ArrayList<>();
        artifacts.add(artifact);

        BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder()
                .id(Sequence.nextBase32Id())
                .buildConfigurationAudited(buildConfigAud)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(testUser)
                .temporaryBuild(false);

        BuildRecord savedRecord = datastore
                .storeCompletedBuild(buildRecordBuilder, artifacts, new ArrayList<>(), new ArrayList<>());

        return savedRecord.getBuiltArtifacts().iterator().next();
    }
}
