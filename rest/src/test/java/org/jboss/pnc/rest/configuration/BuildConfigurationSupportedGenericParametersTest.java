/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.configuration;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class BuildConfigurationSupportedGenericParametersTest {

    private static final String CUSTOM_PME_PARAMETERS = "CUSTOM_PME_PARAMETERS";

    private BuildConfigurationSupportedGenericParameters bcSupportedGenericParameters;

    public BuildConfigurationSupportedGenericParametersTest() throws FileNotFoundException, IOException {
        bcSupportedGenericParameters = new BuildConfigurationSupportedGenericParameters();
    }

    @Test
    public void testGetPMEParameter() {
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).containsKey(CUSTOM_PME_PARAMETERS);
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters().get(CUSTOM_PME_PARAMETERS))
                .startsWith("Additional");
    }
}
