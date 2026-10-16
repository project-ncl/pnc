/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import java.util.Comparator;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;

public class QuicksortTest {

    @Test
    public void quiksortTest() {
        List<Integer> toSort = Lists.list(10, -15, 30, 10, 120, -1000);
        Quicksort.quicksort(toSort, Comparator.naturalOrder());

        for (int a = 0; a < toSort.size() - 1; a++) {
            Assertions.assertThat(toSort.get(a) < toSort.get(a + 1));
        }
    }
}