/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.BUILD_CATEGORY_KEY;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.DEFAULT_BUILD_CATEGORY;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.withDefaults;

import java.util.Collections;
import java.util.Map;

import org.jboss.pnc.api.enums.BuildCategory;
import org.junit.Test;

public class BuildConfigurationParametersUtilsTest {

    @Test
    public void shouldAddDefaultBuildCategoryWhenParametersAreNull() {
        assertThat(withDefaults(null)).containsExactly(entry(BUILD_CATEGORY_KEY, DEFAULT_BUILD_CATEGORY));
    }

    @Test
    public void shouldAddDefaultBuildCategoryWhenNotSet() {
        Map<String, String> parameters = Collections.singletonMap("BUILDER_POD_MEMORY", "4");

        assertThat(withDefaults(parameters))
                .containsOnly(entry("BUILDER_POD_MEMORY", "4"), entry(BUILD_CATEGORY_KEY, DEFAULT_BUILD_CATEGORY));
        assertThat(parameters).doesNotContainKey(BUILD_CATEGORY_KEY);
    }

    @Test
    public void shouldKeepExplicitBuildCategory() {
        String service = BuildCategory.SERVICE.name();

        assertThat(withDefaults(Collections.singletonMap(BUILD_CATEGORY_KEY, service)))
                .containsExactly(entry(BUILD_CATEGORY_KEY, service));
    }
}
