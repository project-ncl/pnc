/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/15/16 Time: 1:13 PM
 */
public class CollectionUtils {

    public static <T> Collection<T> ofNullableCollection(Collection<T> nullable) {
        return nullable == null ? Collections.emptyList() : nullable;
    }

    public static <T> boolean hasCycle(Collection<T> vertices, Function<T, Collection<T>> neighborExtractor) {
        Map<T, Collection<T>> parents = vertices.stream().collect(toMap(identity(), v -> new ArrayList<>()));
        vertices.forEach(
                v -> nullSafeCollection(neighborExtractor.apply(v)).forEach(child -> parents.get(child).add(v)));

        Set<T> roots = vertices.stream().filter(v -> parents.get(v).isEmpty()).collect(Collectors.toSet());

        while (!roots.isEmpty()) {
            T root = roots.iterator().next();
            roots.remove(root);
            parents.remove(root);

            nullSafeCollection(neighborExtractor.apply(root)).forEach(child -> {
                parents.get(child).remove(root);
                if (parents.get(child).isEmpty()) {
                    roots.add(child);
                }
            });

        }

        return !parents.isEmpty();
    }

    public static <T> Collection<T> nullSafeCollection(Collection<T> source) {
        return source == null ? Collections.emptyList() : source;
    }
}
