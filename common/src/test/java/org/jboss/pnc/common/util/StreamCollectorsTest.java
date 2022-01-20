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

package org.jboss.pnc.common.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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