package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
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
        BuildConfiguration exampleConfiguration = new BuildConfiguration();
        exampleConfiguration.setId(6);

        BuildConfigurationRepository repository = mock(BuildConfigurationRepository.class);
        doReturn(exampleConfiguration).when(repository).findOne(6);

        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);

        BuildCoordinator builder = mock(BuildCoordinator.class);
        BuildTriggerer buildTriggerer = new BuildTriggerer(builder, repository, buildConfigurationSetRepository);

        //when
        buildTriggerer.triggerBuilds(6);

        verify(builder).build(eq(exampleConfiguration)); //TODO validate return ?
    }

}