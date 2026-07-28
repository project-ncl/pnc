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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.api.enums.BuildCategory;

public final class BuildConfigurationParametersUtils {

    public static final String BUILD_CATEGORY_KEY = BuildConfigurationParameterKeys.BUILD_CATEGORY.name();

    public static final String DEFAULT_BUILD_CATEGORY = BuildCategory.STANDARD.name();

    private BuildConfigurationParametersUtils() {
    }

    /**
     * Adds the parameters which are implicit when not set explicitly. Namely, the build category, which falls back to
     * {@link BuildCategory#STANDARD}.
     *
     * @param genericParameters build parameters, nullable
     * @return build parameters with the implicit defaults filled in
     */
    public static Map<String, String> withDefaults(Map<String, String> genericParameters) {
        Map<String, String> parametersWithDefaults = genericParameters == null ? new LinkedHashMap<>()
                : new LinkedHashMap<>(genericParameters);
        parametersWithDefaults.putIfAbsent(BUILD_CATEGORY_KEY, DEFAULT_BUILD_CATEGORY);

        return parametersWithDefaults;
    }
}
