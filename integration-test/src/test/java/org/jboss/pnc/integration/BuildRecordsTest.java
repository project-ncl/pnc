package org.jboss.pnc.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.BuildArtifactProvider;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.jirutka.rsql.parser.RSQLParserException;

@RunWith(Arquillian.class)
public class BuildRecordsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecordId;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildArtifactProvider buildArtifactProvider;

    @Inject
    private BuildRecordProvider buildRecordProvider;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(BuildRecordsTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {

        BuildConfiguration buildConfiguration = buildConfigurationRepository.findAll().iterator().next();

        Artifact artifact = new Artifact();
        artifact.setIdentifier("test");
        artifact.setStatus(ArtifactStatus.BINARY_BUILT);

        BuildRecord buildRecord = new BuildRecord();
        buildRecord.setName("java-apns-1.0.0.Beta5");
        buildRecord.setBuildLog("test");
        buildRecord.setStatus(BuildDriverStatus.SUCCESS);
        buildRecord.setBuildConfiguration(buildConfiguration);

        artifact.setBuildRecord(buildRecord);
        buildRecord.getBuiltArtifacts().add(artifact);

        buildRecord = buildRecordRepository.save(buildRecord);

        buildRecordId = buildRecord.getId();
    }

    @Test
    public void shouldGetAllBuildRecords() {
        // when
        List<BuildRecordRest> buildRecords = (List<BuildRecordRest>) buildRecordProvider.getAllArchived(null, null, null, null,
                null);

        // then
        assertThat(buildRecords).isNotNull();
        assertThat(buildRecords.size() > 1);
    }

    @Test
    public void shouldGetSpecificBuildResult() {
        // when
        BuildRecordRest buildRecords = buildRecordProvider.getSpecific(buildRecordId);

        // then
        assertThat(buildRecords).isNotNull();
    }

    @Test
    public void shouldGetLogsForSpecificBuildResult() {
        // when
        StreamingOutput logs = buildRecordProvider.getLogsForBuildId(buildRecordId);

        // then
        assertThat(logs).isNotNull();
    }

    @Test
    public void shouldGetArtifactsForSpecificBuildResult() {
        // when
        List<ArtifactRest> artifacts = buildArtifactProvider.getAll(buildRecordId);

        // then
        assertThat(artifacts).hasSize(1);
    }

    @Test
    public void shouldGetBuildRecordByName() throws RSQLParserException {
        // given
        String rsqlQuery = "name==java-apns-1.0.0.Beta5";

        // when
        List<BuildRecord> buildRecords = selectBuildRecords(rsqlQuery);

        // then
        assertThat(buildRecords).hasSize(1);
    }

    @Test
    public void shouldNotGetBuildRecordByWrongName() throws RSQLParserException {
        // given
        String rsqlQuery = "name==not-existing-br-name";

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

}
