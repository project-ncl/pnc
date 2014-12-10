package org.jboss.pnc.rest.mapping;

import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.ProjectBuildConfigurationBuilder;
import org.jboss.pnc.model.builder.ProjectBuilder;
import org.jboss.pnc.rest.assertion.ProjectBuildConfigurationRestAssertion;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperTest {

    @Test
    public void shouldMapProjectBuildConfiguration() throws Exception {
        //given
        ProjectBuilder projectBuilder = ProjectBuilder.newBuilder();
        projectBuilder.name("test");

        ProjectBuildConfigurationBuilder projectBuildConfigurationBuilder = ProjectBuildConfigurationBuilder.newBuilder();
        projectBuildConfigurationBuilder.id(1).identifier("test").project(projectBuilder.build());

        ProjectBuildConfiguration projectBuildConfiguration = projectBuildConfigurationBuilder.build();

        Mapper mapper = new Mapper();

        //when
        ProjectBuildConfigurationRest mappedObject = mapper.mapTo(projectBuildConfiguration, ProjectBuildConfigurationRest.class);

        //then
        ProjectBuildConfigurationRestAssertion.assertThat(mappedObject).hasId(1).hasProjectName("test").hasIdentifier("test");
    }

    @Test
    public void shouldReturnNullOnMappingNull() throws Exception {
        //given
        Mapper mapper = new Mapper();

        //when
        Object mappedObject = mapper.mapTo(null, Object.class);

        //then
        assertThat(mappedObject).isNull();
    }

}