/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
