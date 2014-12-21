package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BuildTriggererTest {

    @Test
    @Ignore //TODO enable
    public void shouldTriggerBuild() throws Exception {
        //given
        ProjectBuildConfiguration exampleConfiguration = new ProjectBuildConfiguration();
        exampleConfiguration.setId(6);

        ProjectBuildConfigurationRepository repository = mock(ProjectBuildConfigurationRepository.class);
        doReturn(exampleConfiguration).when(repository).findOne(6);

        BuildCoordinator builder = mock(BuildCoordinator.class);
        BuildTriggerer buildTriggerer = new BuildTriggerer(builder, repository);

        //when
        buildTriggerer.triggerBuilds(6);

        verify(builder).build(eq(exampleConfiguration)); //TODO validate return ?
    }

}