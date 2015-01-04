package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectBuildResultRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.*;
import org.jboss.pnc.rest.provider.BuildArtifactProvider;
import org.jboss.pnc.rest.provider.BuildResultProvider;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.restmodel.BuildResultRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(Arquillian.class)
public class BuildResultsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildResultId;

    @Inject
    private ProjectBuildResultRepository projectBuildResultRepository;

    @Inject
    private ProjectBuildConfigurationRepository projectBuildConfigurationRepository;

    @Inject
    private BuildArtifactProvider buildArtifactProvider;

    @Inject
    private BuildResultProvider buildResultProvider;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(BuildResultsTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {
        ProjectBuildConfiguration projectBuildConfiguration = projectBuildConfigurationRepository.findAll().iterator().next();

        Artifact artifact = new Artifact();
        artifact.setIdentifier("test");
        artifact.setStatus(ArtifactStatus.BINARY_BUILT);

        ProjectBuildResult projectBuildResult = new ProjectBuildResult();
        projectBuildResult.setBuildLog("test");
        projectBuildResult.setStatus(BuildDriverStatus.SUCCESS);
        projectBuildResult.setProjectBuildConfiguration(projectBuildConfiguration);

        artifact.setProjectBuildResult(projectBuildResult);
        projectBuildResult.getBuiltArtifacts().add(artifact);

        projectBuildResult = projectBuildResultRepository.save(projectBuildResult);

        buildResultId = projectBuildResult.getId();
    }

    @Test
    public void shouldGetAllBuildResults() {
        //when
        List<BuildResultRest> buildResults = buildResultProvider.getAllArchived();

        //then
        assertThat(buildResults).hasSize(1);
    }

    @Test
    public void shouldGetSpecificBuildResult() {
        //when
        BuildResultRest buildResults = buildResultProvider.getSpecific(buildResultId);

        //then
        assertThat(buildResults).isNotNull();
    }

    @Test
    public void shouldGetLogsForSpecificBuildResult() {
        //when
        StreamingOutput logs = buildResultProvider.getLogsForBuildId(buildResultId);

        //then
        assertThat(logs).isNotNull();
    }

    @Test
    public void shouldGetArtifactsForSpecificBuildResult() {
        //when
        List<ArtifactRest> artifacts = buildArtifactProvider.getAll(buildResultId);

        //then
        assertThat(artifacts).hasSize(1);
    }

}
