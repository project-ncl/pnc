/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-17.
 */
public class StreamCollectors {

    /**
     * Collect element(s) into a list and then return the single element of this list. If there is not exactly one
     * element, throw an exception.
     * 
     * @param <T> module config
     * @return
     */
    public static <T> Collector<T, List<T>, T> singletonCollector() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, list -> {
            if (list.size() == 0) {
                return null;
            }
            if (list.size() > 1) {
                throw new IllegalStateException();
            }
            return list.get(0);
        });
    }

    /**
     * Flattening collector. Look at StreamCollectorsTest for example usage
     *
     *
     * @param <T> type of elements in the collections
     * @return flattened list of elements from all of the collections
     */
    public static <T> Collector<Collection<T>, List<T>, List<T>> toFlatList() {
        return Collector.of(ArrayList::new, List::addAll, (left, right) -> {
            left.addAll(right);
            return left;
        });
    }
}
