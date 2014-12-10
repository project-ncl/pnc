package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.rest.assertion.ProjectBuildConfigurationRestAssertion;
import org.jboss.pnc.rest.mapping.Mapper;
import org.jboss.pnc.rest.mapping.ProjectBuildConfigurationRest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BuildConfigurationProviderTest {

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

        BuildConfigurationProvider buildConfigurationProvider = new BuildConfigurationProvider(repository, null, new Mapper());

        //when
        List<ProjectBuildConfigurationRest> buildConfigurations =  buildConfigurationProvider.getAvailableBuildConfigurations();

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

        BuildConfigurationProvider buildConfigurationProvider = new BuildConfigurationProvider(repository, null, new Mapper());

        //when
        ProjectBuildConfigurationRest returnedConfiguration =  buildConfigurationProvider.getSpecificConfiguration(6);

        //then
        ProjectBuildConfigurationRestAssertion.assertThat(returnedConfiguration).hasId(6).hasProjectName("project");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNull() throws Exception {
        //given
        BuildConfigurationProvider buildConfigurationProvider = new BuildConfigurationProvider(null, null, null);

        //when
        buildConfigurationProvider.getSpecificConfiguration(null);
    }

    @Test
    public void shouldThrowExceptionOnNonExistingId() throws Exception {
        //given
        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(null).when(repository).findOne(6);

        BuildConfigurationProvider buildConfigurationProvider = new BuildConfigurationProvider(repository, null, new Mapper());

        //when
        ProjectBuildConfigurationRest returnedConfiguration =  buildConfigurationProvider.getSpecificConfiguration(6);

        //then
        assertThat(returnedConfiguration).isNull();
    }

}