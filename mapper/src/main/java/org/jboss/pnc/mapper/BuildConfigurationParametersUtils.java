/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
