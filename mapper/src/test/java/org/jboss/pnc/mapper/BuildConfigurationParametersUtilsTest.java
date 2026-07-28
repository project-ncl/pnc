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
package org.jboss.pnc.mapper;

import java.util.Collections;
import java.util.Map;

import org.jboss.pnc.api.enums.BuildCategory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.BUILD_CATEGORY_KEY;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.DEFAULT_BUILD_CATEGORY;
import static org.jboss.pnc.mapper.BuildConfigurationParametersUtils.withDefaults;

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
