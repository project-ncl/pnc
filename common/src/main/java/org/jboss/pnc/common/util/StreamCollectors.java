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
