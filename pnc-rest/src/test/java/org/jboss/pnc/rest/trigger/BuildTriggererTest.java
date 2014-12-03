package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BuildTriggererTest {

    @Test
    public void shouldTriggerBuild() throws Exception {
        //given
        ProjectBuildConfiguration exampleConfiguration = new ProjectBuildConfiguration();
        exampleConfiguration.setId(6);

        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(exampleConfiguration).when(repository).findOne(6);

        ProjectBuilder builder = mock(ProjectBuilder.class);
        BuildTriggerer buildTriggerer = new BuildTriggerer(builder, repository);

        //when
        buildTriggerer.triggerBuilds(6);

        verify(builder).buildProject(eq(exampleConfiguration), any(BuildCollection.class));
    }

}