package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.datastore.repositories.ProjectBuildConfigurationRepository;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Consumer;

import static org.mockito.Matchers.any;
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

        ProjectBuilder builder = mock(ProjectBuilder.class);
        BuildTriggerer buildTriggerer = new BuildTriggerer(builder, repository);

        //when
        buildTriggerer.triggerBuilds(6);

        Consumer<TaskStatus> onStatusUpdate = (newStatus) -> {
            //TODO
        };
        Consumer<BuildJobDetails> onComplete = (buildDetails) -> {
            //TODO
        };
        verify(builder).buildProject(eq(exampleConfiguration), any(Consumer.class), any(Consumer.class));
    }

}