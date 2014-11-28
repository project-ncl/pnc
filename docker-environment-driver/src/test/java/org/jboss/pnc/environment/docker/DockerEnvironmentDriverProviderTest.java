package org.jboss.pnc.environment.docker;

import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerEnvironmentDriverProviderTest {

    @Test
    public void shouldReturnListOfSupportedDrivers() {
        //given
        DockerEnvironmentDriver driver = new DockerEnvironmentDriver();
        DockerEnvironmentDriverProvider provider = new DockerEnvironmentDriverProvider(Arrays.asList(driver));

        //when
        List<EnvironmentDriver> availableDrivers = provider.getAvailableDrivers();

        //then
        assertThat(availableDrivers).containsExactly(driver);
    }

}