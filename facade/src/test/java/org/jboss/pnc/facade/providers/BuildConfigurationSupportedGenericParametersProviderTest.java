/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.enums.BuildCategory;
import org.junit.Test;

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
