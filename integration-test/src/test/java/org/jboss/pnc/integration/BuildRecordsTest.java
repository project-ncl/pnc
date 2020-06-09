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

import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierAndSha256;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecord1Id;

    private static Integer buildRecord2Id;

    private static Integer temporaryBuildId1;

    private static Integer temporaryBuildId2;

    private static Integer buildRecordWithArtifactsId;

    private static String buildConfigName;

    private static Artifact builtArtifact1;

    private static Artifact builtArtifact2;

    private static Artifact builtArtifact3;

    private static Artifact importedArtifact1;

    private static Artifact temporaryBuiltArtifact1;

    private static Artifact temporaryBuiltArtifact2;

    private static Integer savedTempRecordId1;

    private static Integer savedTempRecordId2;

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildRecordProvider buildRecordProvider;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Datastore datastore;

    @Inject
    private RSQLPredicateProducer rsqlPredicateProducer;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(BuildRecordsTest.class);
        war.addClass(ArtifactProvider.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .queryById(new IdRev(100, 1));
        buildConfigName = buildConfigurationAudited.getName();
        BuildConfiguration buildConfiguration = buildConfigurationRepository
                .queryById(buildConfigurationAudited.getId());
        TargetRepository targetRepository = targetRepositoryRepository
                .queryByIdentifierAndPath("indy-maven", "builds-untested");

        builtArtifact1 = createDummyArtifact("1", targetRepository);
        builtArtifact2 = createDummyArtifact("2", targetRepository);
        builtArtifact3 = createDummyArtifact("3", targetRepository);

        importedArtifact1 = Artifact.Builder.newBuilder()
                .filename("importedArtifact1.jar")
                .identifier("integration-test:import-artifact1:jar:1.0")
                .targetRepository(targetRepository)
                .md5("md-fake-i1")
                .sha1("sha1-fake-i1")
                .sha256("sha256-fake-i1")
                .importDate(Date.from(Instant.now()))
                .originUrl("http://central/importedArtifact1.jar")
                .build();

        builtArtifact1 = artifactRepository.save(builtArtifact1);
        builtArtifact2 = artifactRepository.save(builtArtifact2);
        builtArtifact3 = artifactRepository.save(builtArtifact3);
        importedArtifact1 = artifactRepository.save(importedArtifact1);

        List<User> users = userRepository.queryAll();
        assertThat(users.size() > 0).isTrue();
        User user = users.get(0);

        BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build complete")
                .repourLog("alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact1)
                .dependency(importedArtifact1)
                .attribute("attributeKey", "attributeValue1")
                .temporaryBuild(false)
                .build();

        buildRecord1 = buildRecordRepository.save(buildRecord1);
        buildRecord1Id = buildRecord1.getId();

        Artifact builtArtifact1FromDb = artifactRepository
                .queryByPredicates(withIdentifierAndSha256(builtArtifact1.getIdentifier(), builtArtifact1.getSha256()));

        BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build complete")
                .repourLog("alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact2)
                .builtArtifact(builtArtifact3)
                .dependency(builtArtifact1FromDb)
                .dependency(importedArtifact1)
                .attribute("attributeKey", "attributeValue2")
                .temporaryBuild(false)
                .build();

        buildRecord2 = buildRecordRepository.save(buildRecord2);

        buildRecord2Id = buildRecord2.getId();

        BuildRecord buildRecordWithArtifacts = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build completed and has some artifacts")
                .repourLog("alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact1)
                .builtArtifact(builtArtifact2)
                .builtArtifact(builtArtifact3)
                .dependency(builtArtifact1FromDb)
                .dependency(importedArtifact1)
                .attribute("attributeKey", "attributeValue2")
                .temporaryBuild(false)
                .build();

        buildRecordWithArtifacts = buildRecordRepository.save(buildRecordWithArtifacts);

        buildRecordWithArtifactsId = buildRecordWithArtifacts.getId();

        /* Temporary builds */
        temporaryBuiltArtifact1 = createDummyArtifact("temp1", targetRepository, Artifact.Quality.TEMPORARY);
        temporaryBuiltArtifact2 = createDummyArtifact("temp2", targetRepository, Artifact.Quality.TEMPORARY);

        temporaryBuiltArtifact1 = artifactRepository.save(temporaryBuiltArtifact1);
        temporaryBuiltArtifact2 = artifactRepository.save(temporaryBuiltArtifact2);

        long epochMonthAgo = Instant.now().getEpochSecond() - 30 * 60 * 60 * 24;

        BuildRecord temporaryBuild1 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("Temporary build 1")
                .repourLog("Alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(new Date(0))
                .startTime(new Date(0))
                .endTime(new Date(epochMonthAgo * 1000))
                .user(user)
                .builtArtifact(temporaryBuiltArtifact1)
                .dependency(importedArtifact1)
                .temporaryBuild(true)
                .build();

        temporaryBuild1 = buildRecordRepository.save(temporaryBuild1);
        temporaryBuildId1 = temporaryBuild1.getId();

        BuildRecord temporaryBuild2 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("Temporary build 1")
                .repourLog("Alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(temporaryBuiltArtifact2)
                .dependency(importedArtifact1)
                .temporaryBuild(true)
                .build();

        temporaryBuild2 = buildRecordRepository.save(temporaryBuild2);
        temporaryBuildId2 = temporaryBuild2.getId();

        Artifact builtArtifact5 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact5:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 7")
                .md5("adsfs6df548w1327cx78he873217df98")
                .sha1("a56asdf87a3cvx231b87987fasd6f5ads4f32sdf")
                .sha256("sad5f64sf87b3cvx2b1v87tr89h7d3f5g432xcz1zv87fawrv23n8796534564er")
                .size(10L)
                .deployPath("/built5")
                .build();
        Artifact builtArtifact6 = Artifact.Builder.newBuilder()
                .identifier("demo:built-artifact6:jar:1.0")
                .targetRepository(targetRepository)
                .filename("demo built artifact 8")
                .md5("md-fake-abcdefg1234")
                .sha1("sha1-fake-abcdefg1234")
                .sha256("sha256-fake-abcdefg1234")
                .size(10L)
                .deployPath("/built6")
                .build();

        builtArtifact5 = artifactRepository.save(builtArtifact5);
        builtArtifact6 = artifactRepository.save(builtArtifact6);

        int nextId = datastore.getNextBuildRecordId();

        BuildRecord tempRecord1 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Timestamp.from(Instant.now().minus(3, ChronoUnit.HOURS)))
                .startTime(Timestamp.from(Instant.now().minus(3, ChronoUnit.HOURS)))
                .endTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .builtArtifact(builtArtifact5)
                .builtArtifact(builtArtifact6)
                .user(user)
                .repourLog("This is a wannabe alignment log.")
                .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfigurationAudited.getBuildEnvironment())
                .scmRepoURL(buildConfigurationAudited.getRepositoryConfiguration().getInternalUrl())
                .scmRevision(buildConfigurationAudited.getScmRevision())
                .executionRootName("org.jboss.pnc:parent")
                .executionRootVersion("1.2.8")
                .temporaryBuild(true)
                .build();

        BuildConfigurationAudited buildConfigAudited2 = buildConfigurationAuditedRepository
                .queryById(new IdRev(101, 1));

        tempRecord1 = buildRecordRepository.save(tempRecord1);
        savedTempRecordId1 = tempRecord1.getId();
        logger.debug("RECOOOORD1 ==" + savedTempRecordId1);

        nextId = datastore.getNextBuildRecordId();
        BuildRecord tempRecord2 = BuildRecord.Builder.newBuilder()
                .id(nextId)
                .buildConfigurationAudited(buildConfigAudited2)
                .submitTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .startTime(Timestamp.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .endTime(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS).minus(30, ChronoUnit.MINUTES)))
                .dependency(builtArtifact5)
                .user(user)
                .buildLog("Very short demo log: The quick brown fox jumps over the lazy dog.")
                .status(BuildStatus.SUCCESS)
                .buildEnvironment(buildConfigAudited2.getBuildEnvironment())
                .executionRootName("org.jboss.pnc:parent")
                .executionRootVersion("1.2.5")
                .temporaryBuild(true)
                .build();

        tempRecord2 = buildRecordRepository.save(tempRecord2);
        savedTempRecordId2 = tempRecord2.getId();

    }

    @Test
    public void shouldGetAllBuildRecords() {
        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider.getAll(0, 999, null, null).getContent();

        // then
        assertThat(buildRecords).isNotNull();
        assertThat(buildRecords.size() > 1);
    }

    @Test
    public void shouldGetSpecificBuildRecord() {
        // when
        BuildRecordRest buildRecords = buildRecordProvider.getSpecific(buildRecord2Id);

        // then
        assertThat(buildRecords).isNotNull();
    }

    @Test
    public void shouldGetLogsForSpecificBuildRecord() {
        // when
        String buildRecordLog = buildRecordProvider.getBuildRecordLog(buildRecord2Id);
        StreamingOutput logs = buildRecordProvider.getLogsForBuild(buildRecordLog);

        // then
        assertThat(logs).isNotNull();
    }

    @Test
    public void shouldGetRepourLogsForSpecificBuildRecord() {
        // when
        String buildRecordLog = buildRecordProvider.getBuildRecordRepourLog(buildRecord2Id);
        StreamingOutput logs = buildRecordProvider.getRepourLogsForBuild(buildRecordLog);

        // then
        assertThat(logs).isNotNull();
    }

    @Test
    @Transactional
    public void shouldGetOldTemporaryBuild() {
        // given
        BuildRecord expectedBuildRecord = buildRecordRepository.findByIdFetchAllProperties(temporaryBuildId1);
        long epochTwoWeeksAgo = Instant.now().getEpochSecond() - 14 * 24 * 60 * 60;

        // when
        CollectionInfo<BuildRecordRest> temporaryBuilds = buildRecordProvider
                .getAllTemporaryOlderThanTimestamp(0, 10, null, null, epochTwoWeeksAgo * 1000);

        // then
        assertThat(temporaryBuilds).isNotNull();
        assertThat(temporaryBuilds.getContent())
                .usingElementComparatorIgnoringFields("user", "buildConfigurationAudited", "project")
                .containsOnly(toRestBuildRecord(expectedBuildRecord));
    }

    @Test
    public void testIgnoresIfImplicitDependencyIsOlderThanTimestamp() {
        // given

        // Temp Record with endtime -2h
        BuildRecord expectedBuildRecord1 = buildRecordRepository.findByIdFetchAllProperties(savedTempRecordId1);
        // Implicit Dependency with endtime -1h 30m
        BuildRecord expectedBuildRecord2 = buildRecordRepository.findByIdFetchAllProperties(savedTempRecordId2);
        // Older than -1h 45m
        long timestamp = Instant.now().minus(1, ChronoUnit.HOURS).minus(45, ChronoUnit.MINUTES).getEpochSecond();

        // when
        CollectionInfo<BuildRecordRest> temporaryBuilds = buildRecordProvider
                .getAllTemporaryOlderThanTimestamp(0, 10, null, null, timestamp * 1000);

        // then
        assertThat(temporaryBuilds).isNotNull();
        assertThat(temporaryBuilds.getContent()).usingElementComparatorOnFields("id")
                .doesNotContain(toRestBuildRecord(expectedBuildRecord1), toRestBuildRecord(expectedBuildRecord2));
    }

    @Test
    public void testGetTemporaryOlderWithImplicitDependency() {
        // given

        // Temp Record with endtime -2h
        BuildRecord expectedBuildRecord1 = buildRecordRepository.findByIdFetchAllProperties(savedTempRecordId1);
        // Implicit Dependency with endtime -1h 30m
        BuildRecord expectedBuildRecord2 = buildRecordRepository.findByIdFetchAllProperties(savedTempRecordId2);
        // Older than -1h 15m
        long timestamp = Instant.now().minus(1, ChronoUnit.HOURS).minus(15, ChronoUnit.MINUTES).getEpochSecond();

        // when
        CollectionInfo<BuildRecordRest> temporaryBuilds = buildRecordProvider
                .getAllTemporaryOlderThanTimestamp(0, 10, null, null, timestamp * 1000);

        // then
        assertThat(temporaryBuilds).isNotNull();
        assertThat(temporaryBuilds.getContent()).usingElementComparatorOnFields("id")
                .contains(toRestBuildRecord(expectedBuildRecord1), toRestBuildRecord(expectedBuildRecord2));
    }

    @Test
    public void shouldGetOnlyDependencyArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider
                .getDependencyArtifactsForBuildRecord(0, 999, null, null, buildRecord2Id)
                .getContent();

        // then
        assertThat(artifacts).hasSize(2);
    }

    @Test
    public void shouldGetOnlyMinimizedDependencyArtifacts() {
        // when
        // making so that offset is not 0 to avoid org.hsqldb.HsqlException with offset equal to 0
        Collection<ArtifactRest> artifacts = artifactProvider
                .getDependencyArtifactsForBuildRecordMinimized(1, 1, buildRecord2Id)
                .getContent();

        // then
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts).are(new HasEmptyBuildRecordsCollections());
    }

    @Test
    public void shouldGetOnlyBuiltArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 999, null, null, buildRecord2Id)
                .getContent();

        // then
        assertThat(artifacts).hasSize(2);
        assertThat(artifacts).are(new IsBuilt());
    }

    @Test
    public void shouldGetOnlyMinimizedBuiltArtifacts() {
        // when
        // making so that offset is not 0 to avoid org.hsqldb.HsqlException with offset equal to 0
        Collection<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecordMinimized(1, 1, buildRecord2Id)
                .getContent();

        // then
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts).are(new HasEmptyBuildRecordsCollections());
    }

    @Test
    public void shouldGetBuiltArtifactsSortedByFilename() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, "=asc=filename", null, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(
                        toRestArtifact(builtArtifact1),
                        toRestArtifact(builtArtifact2),
                        toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldSortBuiltArtifactsById() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, "=asc=id", null, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(
                        toRestArtifact(builtArtifact1),
                        toRestArtifact(builtArtifact2),
                        toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldFilterBuiltArtifactsByFilename() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(
                0,
                100,
                null,
                "filename==builtArtifact2.jar",
                buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(toRestArtifact(builtArtifact2));
    }

    @Test
    public void shouldFilterBuiltArtifactsByFilenameIdOrChecksum() {

        String builtArtifact1Sha256 = builtArtifact1.getSha256();
        Integer builtArtifact2Id = builtArtifact2.getId();
        String builtArtifact3Filename = builtArtifact3.getFilename();

        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(
                0,
                100,
                null,
                "id==" + builtArtifact2Id + " or sha256==" + builtArtifact1Sha256 + " or filename=="
                        + builtArtifact3Filename,
                buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .contains(
                        toRestArtifact(builtArtifact1),
                        toRestArtifact(builtArtifact2),
                        toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldFilterBuiltArtifactsByFilenameIdAndChecksum() {
        String builtArtifact1Sha256 = builtArtifact1.getSha256();
        Integer builtArtifact1Id = builtArtifact1.getId();
        String builtArtifact1Filename = builtArtifact1.getFilename();

        String matchingFilter = "id==" + builtArtifact1Id + " and sha256==" + builtArtifact1Sha256 + " and filename=="
                + builtArtifact1Filename;
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, null, matchingFilter, buildRecordWithArtifactsId);
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(toRestArtifact(builtArtifact1));

        String builtArtifact2Sha256 = builtArtifact2.getSha256();
        String builtArtifact3Filename = builtArtifact3.getFilename();

        String nonMatchingFilter = "id==" + builtArtifact1Id + " and sha256==" + builtArtifact2Sha256
                + " and filename==" + builtArtifact3Filename;
        artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, null, nonMatchingFilter, buildRecordWithArtifactsId);
        assertThat(artifacts.getContent()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyCollectionForBuiltArtifactsWhenBuildRecordIsNotFound() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, null, null, 123456789);

        // then
        assertThat(artifacts.getContent().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnAllWithoutFilterAndSort() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 100, null, null, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .contains(
                        toRestArtifact(builtArtifact1),
                        toRestArtifact(builtArtifact2),
                        toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldPaginateartifactsProperly() {
        Integer builtArtifact1Id = builtArtifact1.getId();
        String builtArtifact2Sha256 = builtArtifact2.getSha256();

        // when
        String query = "id==" + builtArtifact1Id + " or sha256==" + builtArtifact2Sha256;
        CollectionInfo<ArtifactRest> artifacts = artifactProvider
                .getBuiltArtifactsForBuildRecord(0, 1, null, query, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(toRestArtifact(builtArtifact1));
        assertThat(artifacts.getTotalPages()).isEqualTo(2);

        // when
        artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(1, 1, null, query, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent())
                .usingElementComparatorIgnoringFields("targetRepository", "buildRecordIds", "dependantBuildRecordIds")
                .containsExactly(toRestArtifact(builtArtifact2));
        assertThat(artifacts.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void shouldGetBuildRecordByName() {
        // when
        List<BuildRecordRest> buildRecords = selectBuildRecords(buildConfigName);

        // then
        assertThat(buildRecords).hasAtLeastOneElementOfType(BuildRecordRest.class);
    }

    @Test
    public void shouldNotGetBuildRecordByWrongName() {
        // when
        List<BuildRecordRest> buildRecords = selectBuildRecords("not-existing-br-name");

        // then
        assertThat(buildRecords).isEmpty();
    }

    @Test
    public void shouldGetBuildsInDistributedRecordsetOfProductMilestone() {
        CollectionInfo<BuildRecordRest> buildRecords = buildRecordProvider
                .getAllBuildRecordsWithArtifactsDistributedInProductMilestone(0, 50, null, null, 100);

        assertThat(buildRecords.getContent().iterator().next().getId()).isEqualTo(1);
    }

    @Test
    public void shouldGetBuildRecordAttributes() {
        // given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldGetBuildRecordAttributes-1", "true");
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldGetBuildRecordAttributes-2", "true");
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldGetBuildRecordAttributes-3", "true");

        // when
        Map<String, String> attributes = buildRecordProvider.getAttributes(buildRecord1Id);

        // then
        assertTrue(attributes.size() > 2);
        assertEquals("true", attributes.get("shouldGetBuildRecordAttributes-1"));
    }

    @Test
    public void shouldGetBuildRecordByAttribute() {
        // given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldGetBuildRecordByAttribute-2", "true");

        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider
                .getByAttribute("shouldGetBuildRecordByAttribute-2", "true");

        // then
        assertThat(buildRecords).hasSize(1);
    }

    @Test
    public void shouldNotGetBuildRecordWithoutAttribute() {
        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider.getByAttribute("missing", "true");

        // then
        assertThat(buildRecords).hasSize(0);
    }

    @Test
    public void shouldPutAttributeToBuildRecord() {
        // given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldPutAttributeToBuildRecord-3", "true");
        buildRecordProvider.putAttribute(buildRecord2Id, "shouldPutAttributeToBuildRecord-3", "true");

        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider
                .getByAttribute("shouldPutAttributeToBuildRecord-3", "true");

        // then
        assertThat(buildRecords).hasSize(2);
    }

    @Test
    public void shouldRemoveAttributeFromBuildRecord() {
        // given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldRemoveAttributeFromBuildRecord", "true");
        buildRecordProvider.putAttribute(buildRecord2Id, "shouldRemoveAttributeFromBuildRecord", "true");

        Collection<BuildRecordRest> buildRecords = buildRecordProvider
                .getByAttribute("shouldRemoveAttributeFromBuildRecord", "true");
        assertThat(buildRecords).hasSize(2);

        buildRecordProvider.removeAttribute(buildRecord1Id, "shouldRemoveAttributeFromBuildRecord");
        buildRecordProvider.removeAttribute(buildRecord2Id, "shouldRemoveAttributeFromBuildRecord");

        // when
        buildRecords = buildRecordProvider.getByAttribute("shouldRemoveAttributeFromBuildRecord", "true");

        // then
        assertThat(buildRecords).hasSize(0);
    }

    public static Artifact createDummyArtifact(
            String suffix,
            TargetRepository targetRepository,
            Artifact.Quality quality) {
        return Artifact.Builder.newBuilder()
                .filename("builtArtifact" + suffix + ".jar")
                .identifier("integration-test:built-artifact" + suffix + ":jar:1.0")
                .targetRepository(targetRepository)
                .md5("md-fake-" + suffix)
                .sha1("sha1-fake-" + suffix)
                .sha256("sha256-fake-" + suffix)
                .artifactQuality(quality)
                .build();
    }

    public static Artifact createDummyArtifact(String suffix, TargetRepository targetRepository) {
        return createDummyArtifact(suffix, targetRepository, Artifact.Quality.NEW);
    }

    private List<BuildRecordRest> selectBuildRecords(String buildConfigName) {
        return buildRecordProvider.getAllForConfigurationOrProjectName(0, 999, null, null, buildConfigName)
                .getContent()
                .stream()
                .collect(Collectors.toList());
    }

    private ArtifactRest toRestArtifact(Artifact artifact) {
        return new ArtifactRest(artifact);
    }

    private BuildRecordRest toRestBuildRecord(BuildRecord buildRecord) {
        return new BuildRecordRest(buildRecord);
    }

    class IsImported extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return (artifactRest.getOriginUrl() != null && !artifactRest.getOriginUrl().isEmpty());
        }
    }

    class IsBuilt extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getBuildRecordIds().size() > 0;
        }
    }

    class HasEmptyBuildRecordsCollections extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getBuildRecordIds().isEmpty() && artifactRest.getDependantBuildRecordIds().isEmpty();
        }
    }

}
