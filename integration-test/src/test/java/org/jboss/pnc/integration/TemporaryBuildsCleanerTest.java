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

import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleaner;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.integration.mock.RemoteBuildsCleanerMock;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.exception.ValidationException;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.setup.Deployments.addBuildExecutorMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Jakub Bartecek
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class TemporaryBuildsCleanerTest {
    protected static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private TemporaryBuildsCleaner temporaryBuildsCleaner;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private ProductVersionRepository productVersionRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private Datastore datastore;

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserTransaction transaction;

    private BuildConfigurationAudited buildConfigurationAudited = null;

    private BuildConfigurationSet buildConfigurationSet = null;

    private User user = null;

    private TargetRepository targetRepository = null;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.testEarForInContainerTest(TemporaryBuildsCleanerTest.class);

        JavaArchive coordinator = enterpriseArchive.getAsType(JavaArchive.class, Deployments.COORDINATOR_JAR);
        coordinator.addAsManifestResource("beans-use-mock-remote-clients.xml", "beans.xml");
        coordinator.addClass(RemoteBuildsCleanerMock.class);

        addBuildExecutorMock(enterpriseArchive);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void init() throws Exception {
        if (this.user == null) {
            this.user = userRepository.queryAll().get(0);
            assertNotNull(this.user);
        }

        if (this.buildConfigurationAudited == null) {
            BuildConfiguration buildConfiguration = buildConfigurationRepository.queryAll().get(0);
            this.buildConfigurationAudited = buildConfigurationAuditedRepository
                    .findAllByIdOrderByRevDesc(buildConfiguration.getId())
                    .get(0);
            assertNotNull(this.buildConfigurationAudited);
        }

        if (this.targetRepository == null) {
            this.targetRepository = targetRepositoryRepository.queryAll().get(0);
            assertNotNull(this.targetRepository);
        }

        if (this.buildConfigurationSet == null) {
            transaction.begin(); // required to lazy load the productVersion
            entityManager.joinTransaction();
            this.buildConfigurationSet = buildConfigurationSetRepository.queryAll().get(0);
            assertNotNull(this.buildConfigurationSet);
            buildConfigurationSet.getProductVersion();
            transaction.commit();
        }
    }

    @Test(expected = ValidationException.class)
    public void shouldNotDeleteNonTemporaryBuildTest() throws ValidationException {
        // given
        BuildRecord nonTempBr = initBuildRecordBuilder().temporaryBuild(false).build();
        buildRecordRepository.save(nonTempBr);

        // when - then
        temporaryBuildsCleaner.deleteTemporaryBuild(nonTempBr.getId(), "");

        fail("Deletion of non-temporary build should be prohibited");
    }

    @Test
    public void shouldDeleteTemporaryBuildWithoutArtifactsTest() throws ValidationException {
        // given
        BuildRecord tempBr = initBuildRecordBuilder().temporaryBuild(true).build();
        buildRecordRepository.save(tempBr);
        System.out.println("shouldDeleteTemporaryBuildWithoutArtifactsTest#Inserted BR: " + tempBr.getId());

        List<BuildRecord> givenBuilds = buildRecordRepository.queryAll();
        int numberOfBuilds = givenBuilds.size();

        // when
        temporaryBuildsCleaner.deleteTemporaryBuild(tempBr.getId(), "");

        // then
        assertEquals(numberOfBuilds - 1, buildRecordRepository.queryAll().size());
        assertNull(buildRecordRepository.queryById(tempBr.getId()));
    }

    @Test
    public void shouldDeleteTemporaryBuildWithArtifactsTest() throws SystemException, NotSupportedException,
            HeuristicRollbackException, HeuristicMixedException, RollbackException, ValidationException {
        // given
        Artifact artifact1 = storeAndGetArtifact();
        Artifact artifact2 = storeAndGetArtifact();
        Artifact artifact3 = storeAndGetArtifact();
        Artifact artifact4 = storeAndGetArtifact();

        Set<Artifact> dependencies = new HashSet<>();
        dependencies.add(artifact3);
        dependencies.add(artifact4);

        BuildRecord tempBr = initBuildRecordBuilder().temporaryBuild(true).build();

        tempBr.setDependencies(dependencies);
        BuildRecord savedTempBr = buildRecordRepository.save(tempBr);
        artifact1.setBuildRecord(savedTempBr);
        artifactRepository.save(artifact1);
        artifact2.setBuildRecord(savedTempBr);
        artifactRepository.save(artifact2);

        List<BuildRecord> givenBuilds = buildRecordRepository.queryAll();
        int numberOfBuilds = givenBuilds.size();

        // when
        temporaryBuildsCleaner.deleteTemporaryBuild(tempBr.getId(), "");

        // then
        assertEquals(numberOfBuilds - 1, buildRecordRepository.queryAll().size());
        assertNull(buildRecordRepository.queryById(tempBr.getId()));
        assertNull(artifactRepository.queryById(artifact1.getId()));
        assertNull(artifactRepository.queryById(artifact2.getId()));
        assertNotNull(artifactRepository.queryById(artifact3.getId()));
        assertNotNull(artifactRepository.queryById(artifact4.getId()));
    }

    @Test
    public void shouldNotDeleteNonTemporaryArtifacts() {
        // given
        Artifact artifact = initArtifactBuilder().artifactQuality(ArtifactQuality.NEW).build();
        artifactRepository.save(artifact);

        Set<Artifact> builtArtifacts = new HashSet<>();
        builtArtifacts.add(artifact);

        BuildRecord tempBr = initBuildRecordBuilder().temporaryBuild(true).build();
        BuildRecord savedTempBr = buildRecordRepository.save(tempBr);
        artifact.setBuildRecord(savedTempBr);
        artifactRepository.save(artifact);

        // when - then
        try {
            temporaryBuildsCleaner.deleteTemporaryBuild(tempBr.getId(), "");
        } catch (Exception ex) {
            logger.info("Received exception:", ex);
            if (ex.getCause().getClass().equals(PersistenceException.class)) {
                return;
            }
        }

        fail("Deletion of non-temporary artifacts should be prohibited");
    }

    @Test
    public void shouldReturnOnlyTopLevelTemporaryBuilds() throws Exception {
        // with
        // top level BR1
        BuildRecord br1 = initBuildRecordBuilder().temporaryBuild(true).build();
        br1 = buildRecordRepository.save(br1);

        Artifact art1br1 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br1).build();
        Artifact art2br1 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br1).build();
        artifactRepository.save(art1br1);
        artifactRepository.save(art2br1);

        Set<Artifact> depArtBr2 = new HashSet<>();

        // independent BR2
        BuildRecord br2 = initBuildRecordBuilder().temporaryBuild(true).dependencies(depArtBr2).build();
        br2 = buildRecordRepository.save(br2);

        Artifact art1br2 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br2).build();
        Artifact art2br2 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br2).build();
        artifactRepository.save(art1br2);
        artifactRepository.save(art2br2);

        Set<Artifact> depArtBr3 = new HashSet<>();
        depArtBr3.add(art1br1);
        depArtBr3.add(art1br2);

        // create implicitly dependent BR3 (BR3 is dependent on BR1)
        BuildRecord br3 = initBuildRecordBuilder().temporaryBuild(true).dependencies(depArtBr3).build();
        br3 = buildRecordRepository.save(br3);

        Artifact art1br3 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br3).build();
        Artifact art2br3 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br3).build();
        artifactRepository.save(art1br3);
        artifactRepository.save(art2br3);

        Set<Artifact> depArtBr4 = new HashSet<>();
        depArtBr4.add(art2br2);
        depArtBr4.add(art1br3);

        // create implicitly dependent BR4 (BR4 is dependent on BR2 and BR3)
        BuildRecord br4 = initBuildRecordBuilder().temporaryBuild(true).dependencies(depArtBr4).build();
        br4 = buildRecordRepository.save(br4);

        Artifact art1br4 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br4).build();
        Artifact art2br4 = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).buildRecord(br4).build();
        artifactRepository.save(art1br4);
        artifactRepository.save(art2br4);

        BuildRecord finalBr1 = br1;
        Condition<Build> hasBr1 = new Condition<>(
                (build -> build.getId().equals(BuildMapper.idMapper.toDto(finalBr1.getId()))),
                "Is Br1 with id " + BuildMapper.idMapper.toDto(finalBr1.getId()));

        BuildRecord finalBr2 = br2;
        Condition<Build> hasBr2 = new Condition<>(
                (build -> build.getId().equals(BuildMapper.idMapper.toDto(finalBr2.getId()))),
                "Is Br2 with id " + BuildMapper.idMapper.toDto(finalBr2.getId()));

        BuildRecord finalBr3 = br3;
        Condition<Build> hasBr3 = new Condition<>(
                (build -> build.getId().equals(BuildMapper.idMapper.toDto(finalBr3.getId()))),
                "Is Br3 with id " + BuildMapper.idMapper.toDto(finalBr3.getId()));

        BuildRecord finalBr4 = br4;
        Condition<Build> hasBr4 = new Condition<>(
                (build -> build.getId().equals(BuildMapper.idMapper.toDto(finalBr4.getId()))),
                "Is Br4 with id " + BuildMapper.idMapper.toDto(finalBr4.getId()));

        // when #1
        Page<Build> builds = buildProvider
                .getAllIndependentTemporaryOlderThanTimestamp(0, 50, null, null, new Date().getTime());

        // then #1
        assertThat(builds.getContent()).doNotHave(hasBr1).doNotHave(hasBr2).doNotHave(hasBr3).haveExactly(1, hasBr4);

        // when #2
        temporaryBuildsCleaner.deleteTemporaryBuild(br4.getId(), "");
        builds = buildProvider.getAllIndependentTemporaryOlderThanTimestamp(0, 50, null, null, new Date().getTime());

        // then #2
        assertThat(builds.getContent()).doNotHave(hasBr1).doNotHave(hasBr2).haveExactly(1, hasBr3).doNotHave(hasBr4);

        // when #3
        temporaryBuildsCleaner.deleteTemporaryBuild(br3.getId(), "");
        builds = buildProvider.getAllIndependentTemporaryOlderThanTimestamp(0, 50, null, null, new Date().getTime());

        // then #3
        assertThat(builds.getContent()).haveExactly(1, hasBr1)
                .haveExactly(1, hasBr2)
                .doNotHave(hasBr3)
                .doNotHave(hasBr4);
    }

    @Test(expected = ValidationException.class)
    public void shouldNotDeleteNonTemporaryBuildSetTest() throws ValidationException {
        // given
        BuildRecord tempBr = initBuildRecordBuilder().temporaryBuild(true).build();
        buildRecordRepository.save(tempBr);

        Set<BuildRecord> buildRecords = new HashSet<>();
        buildRecords.add(tempBr);

        BuildConfigSetRecord buildConfigSetRecord = initBuildConfigSetRecordBuilder().temporaryBuild(false).build();
        buildConfigSetRecord.setBuildRecords(buildRecords);
        buildConfigSetRecordRepository.save(buildConfigSetRecord);

        // when - then
        temporaryBuildsCleaner.deleteTemporaryBuildConfigSetRecord(buildConfigSetRecord.getId(), "");

        fail("Deletion of non-temporary build should be prohibited");
    }

    @Test
    public void shouldDeleteSingleTemporaryBuildSetTestWithOneBr() throws Exception {
        // given
        BuildConfigSetRecord buildConfigSetRecord = initBuildConfigSetRecordBuilder().temporaryBuild(true).build();
        buildConfigSetRecordRepository.save(buildConfigSetRecord);

        BuildRecord tempBr = initBuildRecordBuilder().temporaryBuild(true)
                .buildConfigSetRecord(buildConfigSetRecord)
                .build();
        buildRecordRepository.save(tempBr);

        // when
        temporaryBuildsCleaner.deleteTemporaryBuildConfigSetRecord(buildConfigSetRecord.getId(), "");

        // then
        assertNull(buildConfigSetRecordRepository.queryById(buildConfigSetRecord.getId()));
        assertNull(buildRecordRepository.queryById(tempBr.getId()).getBuildConfigSetRecord());

    }

    @Test
    public void shouldNotReturnWithNewNoRebuildRecord() throws Exception {
        // given
        BuildRecord tempBr = initBuildRecordBuilder().submitTime(Date.from(Instant.now().minus(2, ChronoUnit.DAYS)))
                .endTime(Date.from(Instant.now().minus(2, ChronoUnit.DAYS)))
                .temporaryBuild(true)
                .build();
        tempBr = buildRecordRepository.save(tempBr);

        BuildRecord tempNRRBr = initBuildRecordBuilder().status(BuildStatus.NO_REBUILD_REQUIRED)
                .noRebuildCause(tempBr)
                .temporaryBuild(true)
                .build();
        tempNRRBr = buildRecordRepository.save(tempNRRBr);

        // when
        Page<Build> builds = buildProvider.getAllIndependentTemporaryOlderThanTimestamp(
                0,
                50,
                null,
                null,
                Date.from(Instant.now().minus(1, ChronoUnit.DAYS)).getTime());

        // then
        assertThat(builds.getContent()).extracting("id", String.class)
                .doesNotContain(
                        BuildMapper.idMapper.toDto(tempBr.getId()),
                        BuildMapper.idMapper.toDto(tempNRRBr.getId()));
    }

    private Artifact storeAndGetArtifact() {
        Artifact artifact = initArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).build();
        return artifactRepository.save(artifact);

    }

    private Artifact.Builder initArtifactBuilder() {
        return Artifact.Builder.newBuilder()
                .identifier("g:a:v" + UUID.randomUUID().toString())
                .targetRepository(this.targetRepository)
                .md5("md5")
                .sha1("sha1")
                .sha256("sha256");

    }

    private BuildRecord.Builder initBuildRecordBuilder() {
        return BuildRecord.Builder.newBuilder()
                .id(Sequence.nextBase32Id())
                .buildConfigurationAudited(this.buildConfigurationAudited)
                .submitTime(new Date())
                .user(this.user)
                .endTime(new Date())
                .status(BuildStatus.SUCCESS);
    }

    private BuildConfigSetRecord.Builder initBuildConfigSetRecordBuilder() {
        return BuildConfigSetRecord.Builder.newBuilder()
                .buildConfigurationSet(this.buildConfigurationSet)
                .startTime(new Date())
                .user(this.user)
                .endTime(new Date())
                .status(BuildStatus.SUCCESS);
    }

}
