/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

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
