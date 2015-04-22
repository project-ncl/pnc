package org.jboss.pnc.integration;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.assertj.core.api.Condition;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.predicates.rsql.RSQLNodeTravellerPredicate;
import org.jboss.pnc.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.*;
import org.jboss.pnc.rest.provider.ArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecordId;

    private static String buildConfigName;

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

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        war.addClass(BuildRecordsTest.class);
        war.addClass(ArtifactProvider.class);

        JavaArchive datastoreJar = enterpriseArchive.getAsType(JavaArchive.class, "/datastore.jar");
        datastoreJar.addClass(ArtifactPredicates.class);
        datastoreJar.addPackage(RSQLNodeTravellerPredicate.class.getPackage());

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }


    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.findAll().iterator().next();
        buildConfigName = buildConfigurationAudited.getName();
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationAudited.getId());

        Artifact builtArtifact = new Artifact();
        builtArtifact.setIdentifier("test");
        builtArtifact.setStatus(ArtifactStatus.BINARY_BUILT);

        Artifact importedArtifact = new Artifact();
        importedArtifact.setIdentifier("test");
        importedArtifact.setStatus(ArtifactStatus.BINARY_IMPORTED);

        List<User> users = userRepository.findAll();
        assertThat(users.size() > 0).isTrue();
        User user = users.get(0);

        BuildRecord buildRecord = new BuildRecord();
        buildRecord.setBuildLog("test");
        buildRecord.setStatus(BuildStatus.SUCCESS);
        buildRecord.setLatestBuildConfiguration(buildConfiguration);
        buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
        buildRecord.setStartTime(Timestamp.from(Instant.now()));
        buildRecord.setEndTime(Timestamp.from(Instant.now()));
        logger.info(user.toString());
        buildRecord.setUser(user);

        builtArtifact.setBuildRecord(buildRecord);
        importedArtifact.setBuildRecord(buildRecord);
        buildRecord.getBuiltArtifacts().add(builtArtifact);
        buildRecord.getBuiltArtifacts().add(importedArtifact);

        buildRecord = buildRecordRepository.save(buildRecord);

        buildRecordId = buildRecord.getId();
    }

    @Test
    public void shouldGetAllBuildRecords() {
        // when
        List<BuildRecordRest> buildRecords = (List<BuildRecordRest>) buildRecordProvider.getAllArchived(0, 999, null, null);

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
        StreamingOutput logs = buildRecordProvider.getLogsForBuildId(buildRecordId);

        // then
        assertThat(logs).isNotNull();
    }

    @Test
    public void shouldGetArtifactsForSpecificBuildRecord() {
        // when
        List<ArtifactRest> artifacts = artifactProvider.getAllForBuildRecord(0, 999, null, null, buildRecordId);

        // then
        assertThat(artifacts).hasSize(2);
    }

    @Test
    public void shouldGetOnlyImportedArtifacts() {
        //given
        String rsqlQuery = "status==BINARY_IMPORTED";

        // when
        List<ArtifactRest> artifacts = artifactProvider.getAllForBuildRecord(0, 999, null, rsqlQuery, buildRecordId);

        // then
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts).are(new IsImported());
    }

    @Test
    public void shouldGetOnlyBuiltArtifacts() {
        //given
        String rsqlQuery = "status==BINARY_BUILT";

        // when
        List<ArtifactRest> artifacts = artifactProvider.getAllForBuildRecord(0, 999, null, rsqlQuery, buildRecordId);

        // then
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts).are(new IsBuilt());
    }

    @Test
    public void shouldGetBuildRecordByName() throws RSQLParserException {
        // given
        String rsqlQuery = "buildConfigurationAudited.name==" + buildConfigName;

        // when
        List<BuildRecord> buildRecords = selectBuildRecords(rsqlQuery);

        // then
        assertThat(buildRecords).hasAtLeastOneElementOfType(BuildRecord.class);
    }

    @Test
    public void shouldNotGetBuildRecordByWrongName() throws RSQLParserException {
        // given
        String rsqlQuery = "buildConfigurationAudited.name==not-existing-br-name";

        // when
        List<BuildRecord> buildRecords = selectBuildRecords(rsqlQuery);

        // then
        assertThat(buildRecords).isEmpty();
    }

    private List<BuildRecord> selectBuildRecords(String rsqlQuery) throws RSQLParserException {

        return nullableStreamOf(
                buildRecordRepository.findAll(RSQLPredicateProducer.fromRSQL(BuildRecord.class, rsqlQuery).get())).collect(
                Collectors.toList());
    }

    class IsImported extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getStatus() == ArtifactStatus.BINARY_IMPORTED;
        }
    }

    class IsBuilt extends Condition<ArtifactRest> {
        @Override
        public boolean matches(ArtifactRest artifactRest) {
            return artifactRest.getStatus() == ArtifactStatus.BINARY_BUILT;
        }
    }

}
