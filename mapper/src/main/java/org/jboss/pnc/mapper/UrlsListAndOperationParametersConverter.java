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

import javax.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Converts between operation parameters, which represent URL and list of URLs.
 */
@ApplicationScoped
public class UrlsListAndOperationParametersConverter {

    private static final String URL_PARAMETER_PREFIX = "url-";

    public static List<String> urlSetFromOperationParameters(Map<String, String> operationParameters) {
        return operationParameters.entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(URL_PARAMETER_PREFIX))
                .sorted(
                        Comparator.comparingInt(
                                entry -> Integer.parseInt(entry.getKey().substring(URL_PARAMETER_PREFIX.length()))))
                .map(Map.Entry::getValue)
                .distinct()
                .collect(Collectors.toList());
    }

    public static Map<String, String> operationParametersFromUrlSet(List<String> urls) {
        Map<String, String> operationParameters = new TreeMap<>();
        int urlIndex = 0;

        for (String url : urls) {
            operationParameters.put(URL_PARAMETER_PREFIX + urlIndex++, url);
        }

        return operationParameters;
    }
}
