/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
