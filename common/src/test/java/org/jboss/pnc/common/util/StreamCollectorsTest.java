/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/15/16 Time: 1:37 PM
 */
public class StreamCollectorsTest {

    @Test
    public void shouldFlattenTwoLists() {
        List<String> listOne = Arrays.asList("one-1", "one-2", "one-3");
        List<String> listTwo = Arrays.asList("two-1", "two-2");

        List<String> actual = Stream.of(listOne, listTwo).collect(StreamCollectors.toFlatList());

        List<String> expected = new ArrayList<>(listOne);
        expected.addAll(listTwo);
        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    public void shouldFlattenOneList() {
        List<String> listOne = Arrays.asList("one-1", "one-2", "one-3");

        List<String> actual = Stream.of(listOne).collect(StreamCollectors.toFlatList());

        assertThat(actual).hasSameElementsAs(listOne);
    }

    @Test
    public void shouldFlattenNoList() {
        List<String> actual = Stream.<List<String>> of().collect(StreamCollectors.toFlatList());

        assertThat(actual).isNotNull().isEmpty();
    }
}