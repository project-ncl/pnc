package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.rest.assertion.ProjectBuildConfigurationRestAssertion;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BuildTriggerProviderTest {

    @Test
    public void shouldGetAllAvailableBuildConfigurations() throws Exception {
        //given
        Project project = new Project();
        project.setName("project");
        ProjectBuildConfiguration exampleConfiguration = new ProjectBuildConfiguration();
        exampleConfiguration.setId(6);
        exampleConfiguration.setProject(project);

        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(Arrays.asList(exampleConfiguration)).when(repository).findAll();

        BuildTriggerProvider buildTriggerProvider = new BuildTriggerProvider(repository);

        //when
        List<ProjectBuildConfigurationRest> buildConfigurations =  buildTriggerProvider.getAvailableBuildConfigurations();

        //then
        assertThat(buildConfigurations).hasSize(1);
        ProjectBuildConfigurationRestAssertion.assertThat(buildConfigurations.get(0)).hasId(6).hasProjectName("project");
    }

    @Test
    public void shouldGetSpecificConfiguration() throws Exception {
        //given
        Project project = new Project();
        project.setName("project");
        ProjectBuildConfiguration exampleConfiguration = new ProjectBuildConfiguration();
        exampleConfiguration.setId(6);
        exampleConfiguration.setProject(project);

        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(exampleConfiguration).when(repository).findOne(6);

        BuildTriggerProvider buildTriggerProvider = new BuildTriggerProvider(repository);

        //when
        ProjectBuildConfigurationRest returnedConfiguration =  buildTriggerProvider.getSpecificConfiguration(6);

        //then
        ProjectBuildConfigurationRestAssertion.assertThat(returnedConfiguration).hasId(6).hasProjectName("project");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNull() throws Exception {
        //given
        BuildTriggerProvider buildTriggerProvider = new BuildTriggerProvider(null);

        //when
        buildTriggerProvider.getSpecificConfiguration(null);
    }

    @Test
    public void shouldThrowExceptionOnNonExistingId() throws Exception {
        //given
        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(null).when(repository).findOne(6);

        BuildTriggerProvider buildTriggerProvider = new BuildTriggerProvider(repository);

        //when
        ProjectBuildConfigurationRest returnedConfiguration =  buildTriggerProvider.getSpecificConfiguration(6);

        //then
        assertThat(returnedConfiguration).isNull();
    }

}