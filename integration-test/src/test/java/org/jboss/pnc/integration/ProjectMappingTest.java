package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProjectRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.ProductVersionProject;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
public class ProjectMappingTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private ProjectRepository projectRepository;

    private int configurationId;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        war.addClass(ProjectMappingTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    public void prepareTestData() {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findAll().get(0);
        configurationId = buildConfiguration.getId();
    }

    @Test
    public void shouldRemapProjectRestToProject() {
        //given
        ProjectRest projectRest = new ProjectRest();
        projectRest.setId(1);
        projectRest.setConfigurationIds(Arrays.asList(2));
        projectRest.setDescription("description");
        projectRest.setIssueTrackerUrl("issueTracker");
        projectRest.setName("name");
        projectRest.setProjectUrl("projectUrl");

        //when
        Project project = projectRest.toProject();
        List<Integer> buildConfigurationIds = project.getBuildConfigurations().stream()
                .map(buildConfiguration -> buildConfiguration.getId())
                .collect(Collectors.toList());
        Set<ProductVersionProject> productVersionIds = project.getProductVersionProjects();

        //than
        assertThat(project.getId()).isEqualTo(1);
        assertThat(project.getDescription()).isEqualTo("description");
        assertThat(project.getIssueTrackerUrl()).isEqualTo("issueTracker");
        assertThat(project.getName()).isEqualTo("name");
        assertThat(project.getProjectUrl()).isEqualTo("projectUrl");
        assertThat(buildConfigurationIds).containsExactly(2);
        assertThat(productVersionIds).isEmpty();
    }

    @Test
    public void shouldRemappedProjectBePersistable() {
        //given
        ProjectRest projectRest = new ProjectRest();
        projectRest.setConfigurationIds(Arrays.asList(configurationId));
        projectRest.setDescription("description");
        projectRest.setIssueTrackerUrl("issueTracker");
        projectRest.setName("name");
        projectRest.setProjectUrl("projectUrl");

        //when
        Project savedProject = projectRepository.save(projectRest.toProject());

        //then
        assertThat(savedProject.getId()).isNotNull().isPositive();
    }

}
