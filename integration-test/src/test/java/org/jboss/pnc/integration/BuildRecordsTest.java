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

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withIdentifierAndChecksum;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.BuiltArtifact;
import org.jboss.pnc.model.ImportedArtifact;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.utils.StreamHelper;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecordId;

    private static String buildConfigName;

    @Inject
    private ArtifactRepository artifactRepository;

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
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        war.addClass(BuildRecordsTest.class);
        war.addClass(ArtifactProvider.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }


    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryAll().iterator().next();
        buildConfigName = buildConfigurationAudited.getName();
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(buildConfigurationAudited.getId().getId());

        BuiltArtifact builtArtifact1 = BuiltArtifact.Builder.newBuilder()
                .filename("builtArtifact1.jar")
                .identifier("integration-test:built-artifact1:jar:1.0")
                .repoType(RepositoryType.MAVEN)
                .checksum("abcd1234")
                .build();

        BuiltArtifact builtArtifact2 = BuiltArtifact.Builder.newBuilder()
                .filename("builtArtifact2.jar")
                .identifier("integration-test:built-artifact2:jar:1.0")
                .repoType(RepositoryType.MAVEN)
                .checksum("abcd1234")
                .build();

        BuiltArtifact builtArtifact3 = BuiltArtifact.Builder.newBuilder()
                .filename("builtArtifact3.jar")
                .identifier("integration-test:built-artifact3:jar:1.0")
                .repoType(RepositoryType.MAVEN)
                .checksum("abcd1234")
                .build();

        ImportedArtifact importedArtifact1 = ImportedArtifact.Builder.newBuilder()
                .filename("importedArtifact1.jar")
                .identifier("integration-test:import-artifact1:jar:1.0")
                .repoType(RepositoryType.MAVEN)
                .checksum("abcd1234")
                .downloadDate(Date.from(Instant.now()))
                .originUrl("http://central/importedArtifact1.jar")
                .build();

        builtArtifact1 = (BuiltArtifact)artifactRepository.save(builtArtifact1);
        builtArtifact2 = (BuiltArtifact)artifactRepository.save(builtArtifact2);
        builtArtifact3 = (BuiltArtifact)artifactRepository.save(builtArtifact3);
        importedArtifact1 = (ImportedArtifact)artifactRepository.save(importedArtifact1);

        List<User> users = userRepository.queryAll();
        assertThat(users.size() > 0).isTrue();
        User user = users.get(0);

        BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build complete")
                .status(BuildStatus.SUCCESS)
                .latestBuildConfiguration(buildConfiguration)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact1)
                .dependency(importedArtifact1)
                .build();
                
        buildRecord1 = buildRecordRepository.save(buildRecord1);
        Artifact builtArtifact1FromDb = artifactRepository
                .queryByPredicates(withIdentifierAndChecksum(builtArtifact1.getIdentifier(), builtArtifact1.getChecksum()));

        BuildRecord buildRecord2 = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build complete")
                .status(BuildStatus.SUCCESS)
                .latestBuildConfiguration(buildConfiguration)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact2)
                .builtArtifact(builtArtifact3)
                .dependency(builtArtifact1FromDb)
                .dependency(importedArtifact1)
                .build();

        buildRecord2 = buildRecordRepository.save(buildRecord2);

        buildRecordId = buildRecord2.getId();
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
        BuildRecordRest buildRecords = buildRecordProvider.getSpecific(buildRecordId);

        // then
        assertThat(buildRecords).isNotNull();
    }

    @Test
    public void shouldGetLogsForSpecificBuildRecord() {
        // when
        String buildRecordLog = buildRecordProvider.getBuildRecordLog(buildRecordId);
        StreamingOutput logs = buildRecordProvider.getLogsForBuild(buildRecordLog);

        // then
        assertThat(logs).isNotNull();
    }

    @Test
    public void shouldGetArtifactsForSpecificBuildRecord() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getAllForBuildRecord(0, 999, null, null, buildRecordId).getContent();

        //then
        assertThat(artifacts).hasSize(4);
    }

    @Test
    public void shouldGetOnlyDependencyArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getDependencyArtifactsForBuildRecord(0, 999, null, null, buildRecordId).getContent();

        // then
        assertThat(artifacts).hasSize(2);
    }

    @Test
    public void shouldGetOnlyBuiltArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 999, null, null, buildRecordId).getContent();

        // then
        assertThat(artifacts).hasSize(2);
        assertThat(artifacts).are(new IsBuilt());
    }

    @Test
    public void shouldGetBuildRecordByName() {
        // given
        String rsqlQuery = "buildConfigurationAudited.name==" + buildConfigName;

        // when
        List<BuildRecord> buildRecords = selectBuildRecords(rsqlQuery);

        // then
        assertThat(buildRecords).hasAtLeastOneElementOfType(BuildRecord.class);
    }

    @Test
    public void shouldNotGetBuildRecordByWrongName() {
        // given
        String rsqlQuery = "buildConfigurationAudited.name==not-existing-br-name";

        // when
        List<BuildRecord> buildRecords = selectBuildRecords(rsqlQuery);

        // then
        assertThat(buildRecords).isEmpty();
    }

    @Test
    public void shouldGetBuildsInDistributedRecordsetOfProductMilestone() {
        Collection<Integer> buildRecordIds = buildRecordProvider.getAllBuildsInDistributedRecordsetOfProductMilestone(1);

        assertThat(buildRecordIds.iterator().next()).isEqualTo(1);
    }

    private List<BuildRecord> selectBuildRecords(String rsqlQuery) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, rsqlQuery);
        return StreamHelper.nullableStreamOf(buildRecordRepository.queryWithPredicates(rsqlPredicate)).collect(Collectors.toList());
    }

    class IsImported extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getType().equals(ArtifactType.IMPORTED);
        }
    }

    class IsBuilt extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getType().equals(ArtifactType.BUILT);
        }
    }

}
