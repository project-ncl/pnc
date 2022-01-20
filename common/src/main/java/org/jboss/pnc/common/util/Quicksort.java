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

import java.util.Comparator;
import java.util.List;

/**
 * Quicksort implementation with a Comparator object used for comparison.
 */
public class Quicksort<T> {

    /**
     * Quicksort implementation that takes in a List and a comparator, and sorts the list
     *
     * Unlike the default Java implementation (TimSort), it doesn't check if the comparator violates its contract, which
     * is useful in some cases.
     *
     * @param list list to sort
     * @param comparator comparator to compare data
     * @param <T> generic type
     */
    public static <T> void quicksort(List<T> list, Comparator<T> comparator) {
        quicksort(list, comparator, 0, list.size() - 1);
    }

    private static <T> void quicksort(List<T> list, Comparator<T> comparator, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(list, comparator, begin, end);

            quicksort(list, comparator, begin, partitionIndex - 1);
            quicksort(list, comparator, partitionIndex + 1, end);
        }
    }

    private static <T> int partition(List<T> list, Comparator<T> comparator, int begin, int end) {

        // Choose initial pivot
        T pivot = list.get(end);

        int i = (begin - 1);

        for (int j = begin; j < end; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                T swapTemp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, swapTemp);
            }
        }

        T swapTemp = list.get(i + 1);
        list.set(i + 1, list.get(end));
        list.set(end, swapTemp);

        return i + 1;
    }
}