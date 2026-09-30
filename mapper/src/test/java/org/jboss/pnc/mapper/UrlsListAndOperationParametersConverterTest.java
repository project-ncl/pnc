/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class UrlsListAndOperationParametersConverterTest {

    @Test
    public void testUrlSetFromOperationParameters() {
        // given
        var url0 = "https://fake-url.com";
        var url1 = "https://fake-url.cz";
        var operationParameters = new TreeMap<>(Map.of("url-0", url0, "url-1", url1));
        List<String> expectedMappedUrls = new ArrayList<>(List.of(url0, url1));

        // when
        List<String> actualMappedUrls = UrlsListAndOperationParametersConverter
                .urlSetFromOperationParameters(operationParameters);

        // then
        assertThat(actualMappedUrls).isEqualTo(expectedMappedUrls);
    }

    @Test
    public void testOperationParametersFromUrlSet() {
        // given
        var url0 = "https://super-fake-url.com";
        var url1 = "http://janinko.eu";
        var urls = new ArrayList<>(List.of(url0, url1));
        Map<String, String> expectedOperationParameters = new TreeMap<>(Map.of("url-0", url0, "url-1", url1));

        // when
        Map<String, String> actualOperationParameters = UrlsListAndOperationParametersConverter
                .operationParametersFromUrlSet(urls);

        // then
        assertThat(actualOperationParameters).isEqualTo(expectedOperationParameters);
    }
}