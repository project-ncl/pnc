/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.enums.BuildCategory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildConfigurationSupportedGenericParametersProviderTest {

    private BuildConfigurationSupportedGenericParametersProviderImpl bcSupportedGenericParameters;

    public BuildConfigurationSupportedGenericParametersProviderTest() {
        bcSupportedGenericParameters = new BuildConfigurationSupportedGenericParametersProviderImpl();
    }

    @Test
    public void testGetPMEParameter() {
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).anySatisfy(
                param -> BuildConfigurationParameterKeys.ALIGNMENT_PARAMETERS.name().equals(param.getName()));
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters())
                .anySatisfy(parameter -> parameter.getDescription().startsWith("Additional"));
    }

    @Test
    public void testPossibleBuildCategoryValues() {
        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).anySatisfy(parameter -> {
            assertThat(BuildConfigurationParameterKeys.ALIGNMENT_PARAMETERS.name()).isEqualTo(parameter.name);
            assertThat(parameter.values).isNull();
        });

        assertThat(bcSupportedGenericParameters.getSupportedGenericParameters()).anySatisfy(parameter -> {
            assertThat(BuildConfigurationParameterKeys.BUILD_CATEGORY.name()).isEqualTo(parameter.name);
            assertThat(parameter.values).hasSize(BuildCategory.values().length);
            assertThat(parameter.values).anyMatch(p -> BuildCategory.STANDARD.name().equals(p));
        });
    }
}
