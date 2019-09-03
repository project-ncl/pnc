package org.jboss.pnc.facade.providers;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildConfigurationSupportedGenericParametersProviderTest {
    private static final String CUSTOM_PME_PARAMETERS = "CUSTOM_PME_PARAMETERS";

    private BuildConfigurationSupportedGenericParametersProviderImpl bcSupportedGenericParameters;

    public BuildConfigurationSupportedGenericParametersProviderTest() throws FileNotFoundException, IOException {
        bcSupportedGenericParameters =
                new BuildConfigurationSupportedGenericParametersProviderImpl();
    }

    @Test
    public void testGetPMEParameter() {
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).anySatisfy(param -> CUSTOM_PME_PARAMETERS.equals(param.getName()));
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).anySatisfy(parameter -> parameter.getDescription().startsWith("Additional"));
    }
}
