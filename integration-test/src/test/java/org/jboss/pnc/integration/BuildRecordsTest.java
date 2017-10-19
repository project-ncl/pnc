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
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
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

    private static Integer buildRecordWithArtifactsId;

    private static String buildConfigName;

    private static Artifact builtArtifact1;

    private static Artifact builtArtifact2;

    private static Artifact builtArtifact3;

    private static Artifact importedArtifact1;

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
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryAll().iterator().next();
        buildConfigName = buildConfigurationAudited.getName();
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(buildConfigurationAudited.getId().getId());

        builtArtifact1 = Artifact.Builder.newBuilder()
                .filename("builtArtifact1.jar")
                .identifier("integration-test:built-artifact1:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN)
                .md5("md-fake-1")
                .sha1("sha1-fake-1")
                .sha256("sha256-fake-1")
                .build();

        builtArtifact2 = Artifact.Builder.newBuilder()
                .filename("builtArtifact2.jar")
                .identifier("integration-test:built-artifact2:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN)
                .md5("md-fake-2")
                .sha1("sha1-fake-2")
                .sha256("sha256-fake-2")
                .build();

        builtArtifact3 = Artifact.Builder.newBuilder()
                .filename("builtArtifact3.jar")
                .identifier("integration-test:built-artifact3:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN)
                .md5("md-fake-3")
                .sha1("sha1-fake-3")
                .sha256("sha256-fake-3")
                .build();

        importedArtifact1 = Artifact.Builder.newBuilder()
                .filename("importedArtifact1.jar")
                .identifier("integration-test:import-artifact1:jar:1.0")
                .repoType(ArtifactRepo.Type.MAVEN)
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
                .latestBuildConfiguration(buildConfiguration)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .user(user)
                .builtArtifact(builtArtifact1)
                .dependency(importedArtifact1)
                .attribute("attributeKey", "attributeValue1")
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
                .attribute("attributeKey", "attributeValue2")
                .build();

        buildRecord2 = buildRecordRepository.save(buildRecord2);

        buildRecord2Id = buildRecord2.getId();

        BuildRecord buildRecordWithArtifacts = BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildLog("test build completed and has some artifacts")
                .repourLog("alignment done")
                .status(BuildStatus.SUCCESS)
                .latestBuildConfiguration(buildConfiguration)
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
                .build();

        buildRecordWithArtifacts = buildRecordRepository.save(buildRecordWithArtifacts);

        buildRecordWithArtifactsId = buildRecordWithArtifacts.getId();

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
    @Ignore
    public void shouldGetArtifactsForSpecificBuildRecord() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getAllForBuildRecord(0, 999, null, null, buildRecord2Id).getContent();

        //then
        assertThat(artifacts).hasSize(4);
    }

    @Test
    public void shouldGetOnlyDependencyArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getDependencyArtifactsForBuildRecord(0, 999, null, null, buildRecord2Id).getContent();

        // then
        assertThat(artifacts).hasSize(2);
    }

    @Test
    public void shouldGetOnlyBuiltArtifacts() {
        // when
        Collection<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 999, null, null, buildRecord2Id).getContent();

        // then
        assertThat(artifacts).hasSize(2);
        assertThat(artifacts).are(new IsBuilt());
    }

    @Test
    public void shouldGetBuiltArtifactsSortedByFilename() {
        //when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, "=asc=filename", null, buildRecordWithArtifactsId);
        // then
        //dependents doesn't match as they are set after the build
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").containsExactly(
                toRestArtifact(builtArtifact1),
                toRestArtifact(builtArtifact2),
                toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldSortBuiltArtifactsById() {
        //when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, "=asc=id", null, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").containsExactly(
                toRestArtifact(builtArtifact1),
                toRestArtifact(builtArtifact2),
                toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldFilterBuiltArtifactsByFilename() {
        //when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, "filename==builtArtifact2.jar", buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingFieldByFieldElementComparator().containsExactly(toRestArtifact(builtArtifact2));
    }

    /**
     * TODO enable me
     * the 3rd condition "or field=value" is ignored, probably an issue with rsql parser, try to update the rsql-parser to the latest version
     */
    @Ignore
    @Test
    public void shouldFilterBuiltArtifactsByFilenameIdOrChecksum() {

        String builtArtifact1Sha256 = builtArtifact1.getSha256();
        Integer builtArtifact2Id = builtArtifact2.getId();
        String builtArtifact3Filename = builtArtifact3.getFilename();

        //when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, "id==" +  builtArtifact2Id + " or sha256==" +  builtArtifact1Sha256 + " or filename==" + builtArtifact3Filename, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingFieldByFieldElementComparator().contains(
                toRestArtifact(builtArtifact1),
                toRestArtifact(builtArtifact2),
                toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldFilterBuiltArtifactsByFilenameIdAndChecksum() {
        String builtArtifact1Sha256 = builtArtifact1.getSha256();
        Integer builtArtifact1Id = builtArtifact1.getId();
        String builtArtifact1Filename = builtArtifact1.getFilename();

        String matchingFilter = "id==" + builtArtifact1Id + " and sha256==" + builtArtifact1Sha256 + " and filename==" + builtArtifact1Filename;
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, matchingFilter, buildRecordWithArtifactsId);
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").containsExactly(toRestArtifact(builtArtifact1));

        String builtArtifact2Sha256 = builtArtifact2.getSha256();
        String builtArtifact3Filename = builtArtifact3.getFilename();

        String nonMatchingFilter = "id==" + builtArtifact1Id + " and sha256==" + builtArtifact2Sha256 + " and filename==" + builtArtifact3Filename;
        artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, nonMatchingFilter, buildRecordWithArtifactsId);
        assertThat(artifacts.getContent()).isEmpty();
    }

    @Test
    public void shouldReturnEmptyCollectionForBuiltArtifactsWhenBuildRecordIsNotFound() {
        // when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, null, 123456789);

        // then
        assertThat(artifacts.getContent().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnAllWithoutFilterAndSort() {
        //when
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 100, null, null, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").contains(
                toRestArtifact(builtArtifact1),
                toRestArtifact(builtArtifact2),
                toRestArtifact(builtArtifact3));
    }

    @Test
    public void shouldPaginateartifactsProperly() {
        Integer builtArtifact1Id = builtArtifact1.getId();
        String builtArtifact2Sha256 = builtArtifact2.getSha256();

        //when
        String query = "id==" + builtArtifact1Id + " or sha256==" + builtArtifact2Sha256;
        CollectionInfo<ArtifactRest> artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(0, 1, null, query, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").containsExactly(toRestArtifact(builtArtifact1));
        assertThat(artifacts.getTotalPages()).isEqualTo(2);

        //when
        artifacts = artifactProvider.getBuiltArtifactsForBuildRecord(1, 1, null, query, buildRecordWithArtifactsId);
        // then
        assertThat(artifacts.getContent()).usingElementComparatorIgnoringFields("dependantBuildRecordIds").containsExactly(toRestArtifact(builtArtifact2));
        assertThat(artifacts.getTotalPages()).isEqualTo(2);
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
        CollectionInfo<BuildRecordRest> buildRecords = buildRecordProvider.getAllBuildRecordsWithArtifactsDistributedInProductMilestone(0, 50, null, null, 1);

        assertThat(buildRecords.getContent().iterator().next().getId()).isEqualTo(1);
    }

    @Test
    public void shouldGetBuildRecordAttributes() {
        //given
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
        //given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldGetBuildRecordByAttribute-2", "true");

        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider.getByAttribute("shouldGetBuildRecordByAttribute-2", "true");

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
        //given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldPutAttributeToBuildRecord-3", "true");
        buildRecordProvider.putAttribute(buildRecord2Id, "shouldPutAttributeToBuildRecord-3", "true");

        // when
        Collection<BuildRecordRest> buildRecords = buildRecordProvider.getByAttribute("shouldPutAttributeToBuildRecord-3", "true");

        // then
        assertThat(buildRecords).hasSize(2);
    }

    @Test
    public void shouldRemoveAttributeFromBuildRecord() {
        //given
        buildRecordProvider.putAttribute(buildRecord1Id, "shouldRemoveAttributeFromBuildRecord", "true");
        buildRecordProvider.putAttribute(buildRecord2Id, "shouldRemoveAttributeFromBuildRecord", "true");

        Collection<BuildRecordRest> buildRecords = buildRecordProvider.getByAttribute("shouldRemoveAttributeFromBuildRecord", "true");
        assertThat(buildRecords).hasSize(2);

        buildRecordProvider.removeAttribute(buildRecord1Id, "shouldRemoveAttributeFromBuildRecord");
        buildRecordProvider.removeAttribute(buildRecord2Id, "shouldRemoveAttributeFromBuildRecord");

        // when
        buildRecords = buildRecordProvider.getByAttribute("shouldRemoveAttributeFromBuildRecord", "true");

        // then
        assertThat(buildRecords).hasSize(0);
    }

    private List<BuildRecord> selectBuildRecords(String rsqlQuery) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, rsqlQuery);
        return StreamHelper.nullableStreamOf(buildRecordRepository.queryWithPredicates(rsqlPredicate)).collect(Collectors.toList());
    }

    private ArtifactRest toRestArtifact(Artifact artifact) {
        return new ArtifactRest(artifact);
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

}
