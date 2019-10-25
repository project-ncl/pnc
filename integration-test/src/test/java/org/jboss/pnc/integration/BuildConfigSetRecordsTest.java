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
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.BuildConfigSetRecordProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigSetRecordsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles
            .lookup()
            .lookupClass());

    private static Integer temporaryBuildId1;

    private static Integer temporaryBuildId2;

    private static Integer temporaryBuildSetRecordId1;

    private static Integer temporaryBuildSetRecordId2;

    private static Artifact temporaryBuiltArtifact1;

    private static Artifact temporaryBuiltArtifact2;

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private BuildConfigSetRecordProvider buildConfigSetRecordProvider;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Datastore datastore;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(BuildConfigSetRecordsTest.class);
        war.addClass(BuildRecordsTest.class);


        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(new IdRev
                (100, 1));
        TargetRepository targetRepository = targetRepositoryRepository.queryByIdentifierAndPath("indy-maven",
                "builds-untested");

        List<User> users = userRepository.queryAll();
        assertThat(users.size() > 0).isTrue();
        User user = users.get(0);

        temporaryBuiltArtifact1 = BuildRecordsTest.createDummyArtifact("temp1", targetRepository, Artifact.Quality
                .TEMPORARY);
        temporaryBuiltArtifact2 = BuildRecordsTest.createDummyArtifact("temp2", targetRepository, Artifact.Quality
                .TEMPORARY);

        temporaryBuiltArtifact1 = artifactRepository.save(temporaryBuiltArtifact1);
        temporaryBuiltArtifact2 = artifactRepository.save(temporaryBuiltArtifact2);

        long epochMonthAgo = Instant
                .now()
                .getEpochSecond() - 30 * 60 * 60 * 24;

        System.out.println("Epoch: " + Instant
                .now()
                .getEpochSecond());

        System.out.println("epochMonthAgo: " + epochMonthAgo);

        System.out.println("30 * 60 * 60 * 24: " + (30 * 60 * 60 * 24));
        /* Temporary Build 1  */
        BuildRecord temporaryBuild1 = BuildRecord.Builder
                .newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("Temporary build 1")
                .repourLog("Alignment done")
                .status(BuildStatus.SUCCESS)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(new Date(0))
                .startTime(new Date(0))
                .endTime(new Date(epochMonthAgo))
                .user(user)
                .builtArtifact(temporaryBuiltArtifact1)
                .temporaryBuild(true)
                .build();

        temporaryBuild1 = buildRecordRepository.save(temporaryBuild1);
        temporaryBuildId1 = temporaryBuild1.getId();

        /* Temporary Build 2  */
        BuildRecord temporaryBuild2 = BuildRecord.Builder
                .newBuilder()
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
                .dependency(temporaryBuiltArtifact1)
                .temporaryBuild(true)
                .build();

        temporaryBuild2 = buildRecordRepository.save(temporaryBuild2);
        temporaryBuildId2 = temporaryBuild2.getId();


        /* Temporary Build Set Record 1  */
        assertThat(buildConfigurationSetRepository.count()).isGreaterThan(0);
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository
                .queryAll()
                .get(0);

        BuildConfigSetRecord buildConfigSetRecord1 = BuildConfigSetRecord.Builder
                .newBuilder()
                .buildRecords(Stream
                        .of(temporaryBuild1)
                        .collect(Collectors.toSet()))
                .startTime(new Date(epochMonthAgo * 1000))
                .endTime(new Date(epochMonthAgo * 1000))
                .temporaryBuild(true)
                .status(BuildStatus.SUCCESS)
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .build();

        buildConfigSetRecord1 = buildConfigSetRecordRepository.save(buildConfigSetRecord1);
        temporaryBuildSetRecordId1 = buildConfigSetRecord1.getId();

        /* Temporary Build Set Record 2  */
        BuildConfigSetRecord buildConfigSetRecord2 = BuildConfigSetRecord.Builder
                .newBuilder()
                .buildRecords(Stream
                        .of(temporaryBuild2)
                        .collect(Collectors.toSet()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .temporaryBuild(true)
                .status(BuildStatus.SUCCESS)
                .buildConfigurationSet(buildConfigurationSet)
                .user(user)
                .build();

        buildConfigSetRecord2 = buildConfigSetRecordRepository.save(buildConfigSetRecord2);
        temporaryBuildSetRecordId2 = buildConfigSetRecord2.getId();
    }

    @Test
    @Transactional
    public void shouldGetOldTemporaryBuildSetRecord() {
        // given
        BuildConfigSetRecord expectedBuildConfigSetRecord = buildConfigSetRecordRepository.queryById
                (temporaryBuildSetRecordId1);
        long epochTwoWeeksAgo = Instant
                .now()
                .getEpochSecond() - 14 * 24 * 60 * 60;

        // when
        CollectionInfo<BuildConfigSetRecordRest> temporaryBuilds = buildConfigSetRecordProvider
                .getAllTemporaryOlderThanTimestamp(0, 10, null, null, epochTwoWeeksAgo * 1000);

        // then
        assertThat(temporaryBuilds).isNotNull();
        assertThat(temporaryBuilds.getContent())
                .usingElementComparatorIgnoringFields("buildRecordIds")
                .containsOnly(toBuildConfigSetRecordRest(expectedBuildConfigSetRecord));
    }

    private BuildConfigSetRecordRest toBuildConfigSetRecordRest(BuildConfigSetRecord buildConfigSetRecord) {
        return new BuildConfigSetRecordRest(buildConfigSetRecord);
    }
}
