package org.jboss.pnc.environment.docker.configuration;

import org.jboss.pnc.environment.docker.DockerEnvironmentDriver;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerEnvironmentConfigurationTest {

    @Test
    public void shouldReturnDockerEnvironmentDriver() {
        //given
        DockerEnvironmentConfiguration dockerEnvironmentConfiguration = new DockerEnvironmentConfiguration();

        //when
        DockerEnvironmentDriver dockerEnvironmentDriver = dockerEnvironmentConfiguration.dockerEnvironmentDriver();

        //then
        assertThat(dockerEnvironmentDriver).isExactlyInstanceOf(DockerEnvironmentDriver.class);
    }

}